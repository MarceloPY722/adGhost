package com.adghost.app.webview

object JsInjector {

    fun getEmergencyBlocker(): String {
        return """
(function() {
    'use strict';
    var _realOpen = window.open;
    window.open = function() { return null; };
    
    window.confirm = function() { return false; };
    window.alert = function() {};
    window.prompt = function() { return null; };
    
    window.addEventListener('beforeunload', function(e) {
        e.preventDefault();
        e.stopPropagation();
    }, true);
    
    var _location = window.location;
    var _origHost = window.location.hostname;
    var _lastClickTime = 0;
    
    document.addEventListener('click', function(e) {
        _lastClickTime = Date.now();
        var el = e.target;
        while (el) {
            var onclick = el.getAttribute('onclick') || '';
            var href = el.getAttribute('href') || '';
            if (onclick.indexOf('window') > -1 || onclick.indexOf('location') > -1 || 
                onclick.indexOf('open(') > -1 || href.indexOf('http') === -1 && href.length > 5) {
                e.preventDefault();
                e.stopPropagation();
                return;
            }
            el = el.parentElement;
        }
    }, true);
    
    var _setTimeout = window.setTimeout;
    window.setTimeout = function(fn, delay) {
        if (typeof fn === 'string') {
            var s = fn.toLowerCase();
            if (s.indexOf('location') > -1 || s.indexOf('.href') > -1 || 
                s.indexOf('open(') > -1 || s.indexOf('redirect') > -1) return -1;
            return _setTimeout(fn, delay);
        }
        var fnStr = (fn.toString() || '').toLowerCase();
        if (fnStr.indexOf('location') > -1 || fnStr.indexOf('.href') > -1 ||
            fnStr.indexOf('redirect') > -1 || fnStr.indexOf('open(') > -1) {
            return -1;
        }
        return _setTimeout(fn, delay);
    };
    
    var _setInterval = window.setInterval;
    window.setInterval = function(fn, delay) {
        var fnStr = (fn.toString() || '').toLowerCase();
        if (fnStr.indexOf('location') > -1 || fnStr.indexOf('.href') > -1 ||
            fnStr.indexOf('redirect') > -1 || fnStr.indexOf('open(') > -1 ||
            fnStr.indexOf('popup') > -1) return -1;
        return _setInterval(fn, delay);
    };
    
    new MutationObserver(function(m) {
        m.forEach(function(mut) {
            mut.addedNodes.forEach(function(n) {
                if (n.nodeType === 1) {
                    if (n.tagName === 'SCRIPT') {
                        var src = (n.src || '').toLowerCase();
                        var text = (n.textContent || '').toLowerCase();
                        if (text.indexOf('window.location') > -1 || text.indexOf('location.href') > -1 ||
                            text.indexOf('location.replace') > -1 || text.indexOf('document.location') > -1 ||
                            src.indexOf('acscdn') > -1 || src.indexOf('popads') > -1 ||
                            src.indexOf('propellerads') > -1 || src.indexOf('exoclick') > -1) {
                            n.remove();
                        }
                    }
                    if (n.tagName === 'IFRAME' || n.tagName === 'IMG') {
                        var src = (n.src || '').toLowerCase();
                        if (n.width <= 2 && n.height <= 2) { n.remove(); return; }
                        var adDomains = ['doubleclick','googlesyndication','popads','propellerads','exoclick','acscdn'];
                        if (adDomains.some(function(d) { return src.indexOf(d) > -1; })) { n.remove(); }
                    }
                    if (n.tagName === 'A') {
                        var h = (n.href || '').toLowerCase();
                        var adDomains = ['doubleclick','googlesyndication','popads','propellerads','exoclick','acscdn','adsterra'];
                        if (adDomains.some(function(d) { return h.indexOf(d) > -1; })) {
                            n.remove();
                        }
                    }
                }
            });
        });
    }).observe(document.documentElement, { childList: true, subtree: true });
})();
""".trimIndent()
    }

    fun getAdBlockScript(): String {
        return """
(function() {
    'use strict';
    
    window.open = function() { return null; };
    
    var adKeywords = ['acscdn','popads','propellerads','exoclick','adsterra','clickadu',
        'mgid','trafficfactory','adexchangerapid','popadscdn','adfoc','bannersbro',
        'vidmoly','streamwish','doodstream','filemoon','burst','premium'];
    
    document.querySelectorAll('script').forEach(function(s) {
        var src = (s.src || '').toLowerCase();
        var text = (s.textContent || '').toLowerCase();
        if (adKeywords.some(function(k) { return src.indexOf(k) > -1 || text.indexOf(k) > -1; }) ||
            text.indexOf('window.location') > -1 || text.indexOf('location.href') > -1 ||
            text.indexOf('location.replace') > -1 || text.indexOf('document.location') > -1 ||
            text.indexOf('open(') > -1 || text.indexOf('popunder') > -1 ||
            text.indexOf('runpop') > -1 || text.indexOf('aclib') > -1) {
            s.remove();
        }
    });
    
    var selectors = [
        '[class*="popup"]','[id*="popup"]',
        '[class*="popunder"]','[id*="popunder"]',
        '[class*="overlay"]','[id*="overlay"]',
        '[class*="modal"]','[id*="modal"]',
        '[class*="ad-"]','[id*="ad-"]',
        '[class*="ads-"]','[id*="ads-"]',
        '[class*="banner"]','[id*="banner"]',
        '[class*="sponsor"]','[id*="sponsor"]',
        '[class*="promo"]','[id*="promo"]',
        '.adsbygoogle','.advertisement','.advertising',
        '.ad-container','.ad-wrapper','.ad-block','.adslot',
        '[data-ad]','[data-google-query-id]',
        'ins.adsbygoogle',
        '.fc-ab-root','.fc-dialog-container','.fc-dialog',
        '.gpt-ad','.dfp-ad',
        '[class*="interstitial"]','[id*="interstitial"]',
        '[class*="fullscreen"]','[id*="fullscreen"]',
        '[style*="position: fixed"][style*="z-index: 9999"]',
        '[style*="position: fixed"][style*="z-index: 999"]',
        '[onclick*="window"]','[onclick*="location"]',
        '[onclick*="open("]',
    ];
    
    function killAds() {
        document.querySelectorAll(selectors.join(',')).forEach(function(el) {
            if (el && el.parentNode) { el.remove(); }
        });
        
        document.querySelectorAll('iframe, img').forEach(function(el) {
            var src = (el.src || '').toLowerCase();
            if (el.width <= 2 && el.height <= 2) { el.remove(); return; }
            if (adKeywords.some(function(k) { return src.indexOf(k) > -1; })) { el.remove(); }
        });
        
        document.querySelectorAll('a').forEach(function(a) {
            var h = (a.href || '').toLowerCase();
            if (adKeywords.some(function(k) { return h.indexOf(k) > -1; })) { a.remove(); }
        });
        
        document.querySelectorAll('div, section').forEach(function(el) {
            var style = window.getComputedStyle(el);
            if (style.position === 'fixed' || style.position === 'sticky') {
                var z = parseInt(style.zIndex);
                if (z >= 999 || (style.top === '0px' && style.left === '0px' && style.width === '100%')) {
                    if (el.offsetHeight > 40 || z >= 9999) { el.remove(); }
                }
            }
        });
    }
    
    killAds();
    
    if (!window.__lfAdObserver) {
        window.__lfAdObserver = true;
        new MutationObserver(function(mutations) {
            var needsKill = false;
            mutations.forEach(function(mutation) {
                if (mutation.addedNodes && mutation.addedNodes.length > 0) {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) {
                            if (node.tagName === 'SCRIPT') {
                                var src = (node.src || '').toLowerCase();
                                var text = (node.textContent || '').toLowerCase();
                                if (adKeywords.some(function(k) { 
                                    return src.indexOf(k) > -1 || text.indexOf(k) > -1; 
                                }) || text.indexOf('location') > -1 || text.indexOf('open(') > -1) {
                                    node.remove();
                                }
                            }
                            if (node.tagName === 'IFRAME' || node.tagName === 'IMG') {
                                var src = (node.src || '').toLowerCase();
                                if (adKeywords.some(function(k) { return src.indexOf(k) > -1; }) ||
                                    (node.width <= 2 && node.height <= 2)) {
                                    node.remove();
                                }
                            }
                        }
                    });
                    needsKill = true;
                }
            });
            if (needsKill) killAds();
        }).observe(document.body, { childList: true, subtree: true });
    }
})();
""".trimIndent()
    }

    fun getCssHidingScript(): String {
        return """
(function() {
    'use strict';
    var style = document.createElement('style');
    style.id = '__lf_style';
    style.textContent = `
        ` + String.raw`[class*="popup"], [id*="popup"],
        [class*="popunder"], [id*="popunder"],
        [class*="overlay"]:not([class*="overlay-content"]),
        [id*="overlay"]:not([id*="overlay-content"]),
        [class*="modal"]:not([class*="modal-content"]),
        [id*="modal"]:not([id*="modal-content"]),
        [class*="ad-"]:not([class*="admin"]):not([class*="add"]):not([class*="adap"]):not([class*="adult"]):not([class*="addr"]),
        [id*="ad-"]:not([id*="admin"]):not([id*="add"]):not([id*="addr"]),
        [class*="banner"], [id*="banner"],
        .adsbygoogle, .advertisement, .advertising,
        .ad-container, .ad-wrapper, .ad-block, .adslot,
        .fc-ab-root, .fc-dialog-container, .fc-dialog,
        ins.adsbygoogle,
        [data-ad-format], [data-ad-client], [data-ad-slot],
        iframe[src*="doubleclick"], iframe[src*="googlesyndication"],
        iframe[src*="googleadservices"], iframe[src*="adservice"],
        iframe[src*="popads"], iframe[src*="acscdn"],
        iframe[src*="propellerads"], iframe[src*="exoclick"],
        .interstitial, .interstitial-ad,
        [class*="sponsor"], [id*="sponsor"],
        [style*="z-index: 9999"], [style*="z-index: 999"],
        [onclick*="window"], [onclick*="location"],
        [onclick*="open("],
        iframe[width="1"][height="1"], iframe[width="0"][height="0"],
        img[width="1"][height="1"]
        {
            display: none !important;
            visibility: hidden !important;
            pointer-events: none !important;
            opacity: 0 !important;
            height: 0px !important;
            width: 0px !important;
            position: absolute !important;
            top: -9999px !important;
            left: -9999px !important;
            overflow: hidden !important;
            clip: rect(0,0,0,0) !important;
            margin: 0 !important;
            padding: 0 !important;
            border: none !important;
        }
        a[onclick], a[href*="javascript:"] {
            pointer-events: none !important;
            cursor: default !important;
        }
    `;
    document.head.appendChild(style);
})();
""".trimIndent()
    }
}
