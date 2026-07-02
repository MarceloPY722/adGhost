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
    
    document.addEventListener('click', function(e) {
        var el = e.target;
        while (el) {
            var onclick = el.getAttribute('onclick') || '';
            var href = el.getAttribute('href') || '';
            if (onclick.indexOf('window.location') > -1 || 
                onclick.indexOf('location.href') > -1 ||
                onclick.indexOf('location.replace') > -1 ||
                href.indexOf('javascript:') === 0 || 
                href.indexOf('data:') === 0 ||
                href.indexOf('vbscript:') === 0) {
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
            if ((s.indexOf('popunder') > -1 || s.indexOf('popup') > -1) &&
                (s.indexOf('location') > -1 || s.indexOf('.href') > -1)) return -1;
            return _setTimeout(fn, delay);
        }
        try {
            var fnStr = (fn.toString() || '').toLowerCase();
            if ((fnStr.indexOf('popunder') > -1 || fnStr.indexOf('popup') > -1) &&
                (fnStr.indexOf('location') > -1 || fnStr.indexOf('.href') > -1)) return -1;
        } catch(e) {}
        return _setTimeout(fn, delay);
    };
    
    var _setInterval = window.setInterval;
    window.setInterval = function(fn, delay) {
        try {
            var fnStr = (fn.toString() || '').toLowerCase();
            if ((fnStr.indexOf('popunder') > -1 || fnStr.indexOf('popup') > -1) &&
                (fnStr.indexOf('location') > -1 || fnStr.indexOf('.href') > -1)) return -1;
        } catch(e) {}
        return _setInterval(fn, delay);
    };
    
    new MutationObserver(function(m) {
        m.forEach(function(mut) {
            mut.addedNodes.forEach(function(n) {
                if (n.nodeType === 1) {
                    if (n.tagName === 'SCRIPT') {
                        var src = (n.src || '').toLowerCase();
                        var text = (n.textContent || '').toLowerCase();
                        if (src.indexOf('popads') > -1 || src.indexOf('propellerads') > -1 || 
                            src.indexOf('exoclick') > -1 || src.indexOf('acscdn') > -1) {
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
        if (adKeywords.some(function(k) { return src.indexOf(k) > -1 || text.indexOf(k) > -1; })) {
            s.remove();
        }
    });
    
    var selectors = [
        '[class*="popup"]:not([class*="ytp-"]):not([class*="yt-"]):not([class*="yt-"])',
        '[id*="popup"]:not([id*="yt"])',
        '[class*="popunder"]','[id*="popunder"]',
        '[class*="overlay"]:not([class*="ytp-"]):not([class*="yt-"])',
        '[id*="overlay"]:not([id*="yt"])',
        '[class*="ad-"]:not([class*="admin"]):not([class*="add"]):not([class*="adap"]):not([class*="adult"]):not([class*="addr"]):not([class*="ytp-"]):not([class*="yt-"]):not([class*="ad-"]):not([class*="ad-"]):not([class*="ad-"]),
        '[id*="ad-"]:not([id*="admin"]):not([id*="add"]):not([id*="addr"]):not([id*="yt"])',
        '[class*="ads-"]:not([class*="ytp-"]):not([class*="yt-"])',
        '[id*="ads-"]:not([id*="yt"])',
        '[class*="banner"]:not([class*="ytp-"]):not([class*="yt-"])',
        '[id*="banner"]:not([id*="yt"])',
        '[class*="sponsor"]:not([class*="ytp-"]):not([class*="yt-"])',
        '[id*="sponsor"]:not([id*="yt"])',
        '[class*="promo"]:not([class*="ytp-"]):not([class*="yt-"])',
        '[id*="promo"]:not([id*="yt"])',
        '.adsbygoogle','.advertisement','.advertising',
        '.ad-container','.ad-wrapper','.ad-block','.adslot',
        '[data-ad]','[data-google-query-id]',
        'ins.adsbygoogle',
        '.fc-ab-root','.fc-dialog-container','.fc-dialog',
        '.gpt-ad','.dfp-ad',
        '[class*="interstitial"]','[id*="interstitial"]',
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
                                })) {
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
        ` + String.raw`[class*="popup"]:not([class*="ytp-"]):not([class*="yt-"]),
        [id*="popup"]:not([id*="yt"]),
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
        .interstitial, .interstitial-ad
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
        a[href^="javascript:"], a[href^="data:"], a[href^="vbscript:"] {
            pointer-events: none !important;
            cursor: default !important;
        }
    `;
    document.head.appendChild(style);
})();
""".trimIndent()
    }

    fun getAudioModeScript(): String {
        return """
(function() {
    var videos = document.querySelectorAll('video');
    videos.forEach(function(v) {
        v.muted = false;
        v.volume = 1.0;
        v.removeAttribute('controls');
        v.style.cssText = 'position:fixed !important;left:-9999px !important;top:-9999px !important;width:1px !important;height:1px !important;opacity:0 !important;pointer-events:none !important;z-index:-1 !important;';
        if (v.paused) v.play().catch(function(){});
    });
    var ytPlayer = document.querySelector('#movie_player, .video-stream, .html5-video-player');
    if (ytPlayer) {
        ytPlayer.style.cssText += 'visibility:hidden !important;opacity:0 !important;height:0 !important;overflow:hidden !important;';
    }
    var existing = document.querySelector('#__ag_audio_banner');
    if (!existing) {
        var div = document.createElement('div');
        div.id = '__ag_audio_banner';
        div.style.cssText = 'position:fixed;top:0;left:0;right:0;background:#1a1a2e;color:#4fc3f7;padding:10px 16px;text-align:center;z-index:2147483647;font-size:14px;font-family:sans-serif;display:flex;align-items:center;justify-content:center;gap:8px;';
        div.innerHTML = '<span style="font-size:18px;">&#9835;</span> Modo Audio - Audio Only <span style="font-size:18px;">&#9835;</span>';
        document.body.prepend(div);
    }
})();
""".trimIndent()
    }

    fun getVideoModeScript(): String {
        return """
(function() {
    var videos = document.querySelectorAll('video');
    videos.forEach(function(v) {
        v.style.cssText = '';
        v.muted = false;
        v.volume = 1.0;
    });
    var ytPlayer = document.querySelector('#movie_player, .video-stream, .html5-video-player');
    if (ytPlayer) {
        ytPlayer.style.cssText = '';
    }
    var banner = document.querySelector('#__ag_audio_banner');
    if (banner) banner.remove();
})();
""".trimIndent()
    }
}
