use adblock::engine::Engine;
use adblock::lists::ParseOptions;
use adblock::request::Request;
use jni::JNIEnv;
use jni::objects::{JClass, JByteArray, JString};
use jni::sys::{jboolean, jint, JNI_FALSE, JNI_TRUE};
use std::panic;
use std::sync::Mutex;

static ENGINE: once_cell::sync::Lazy<Mutex<Option<Engine>>> =
    once_cell::sync::Lazy::new(|| Mutex::new(None));

fn log_info(env: &mut JNIEnv, tag: &str, msg: &str) {
    let cls = match env.find_class("android/util/Log") {
        Ok(c) => c,
        Err(_) => return,
    };
    let tag = match env.new_string(tag) {
        Ok(s) => s,
        Err(_) => return,
    };
    let msg = match env.new_string(msg) {
        Ok(s) => s,
        Err(_) => return,
    };
    let _ = env.call_static_method(
        cls,
        "i",
        "(Ljava/lang/String;Ljava/lang/String;)I",
        &[(&tag).into(), (&msg).into()],
    );
}

#[no_mangle]
pub extern "system" fn Java_com_adghost_app_util_NativeAdBlockEngine_nativeInit(
    mut env: JNIEnv,
    _class: JClass,
    data: JByteArray,
) -> jint {
    let result = panic::catch_unwind(panic::AssertUnwindSafe(|| {
        let bytes: Vec<u8> = match env.convert_byte_array(&data) {
            Ok(b) => b,
            Err(_) => return -1,
        };

        log_info(&mut env, "AdGhost-JNI", &format!("Deserializing engine from {} bytes...", bytes.len()));

        let mut engine = Engine::from_rules(vec!["||example.com^"], ParseOptions::default());
        match engine.deserialize(&bytes) {
            Ok(_) => {
                let mut guard = ENGINE.lock().unwrap();
                *guard = Some(engine);
                log_info(&mut env, "AdGhost-JNI", "Engine deserialized successfully");
                0
            }
            Err(e) => {
                log_info(&mut env, "AdGhost-JNI", &format!("Deserialize error: {:?}", e));
                -3
            }
        }
    }));

    match result {
        Ok(code) => code,
        Err(_) => {
            log_info(&mut env, "AdGhost-JNI", "Panic during nativeInit!");
            -2
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_adghost_app_util_NativeAdBlockEngine_nativeShouldBlock(
    mut env: JNIEnv,
    _class: JClass,
    url: JString,
    source_url: JString,
    request_type: JString,
) -> jboolean {
    let result = panic::catch_unwind(panic::AssertUnwindSafe(|| {
        let guard = match ENGINE.lock() {
            Ok(g) => g,
            Err(poisoned) => poisoned.into_inner(),
        };

        if let Some(engine) = guard.as_ref() {
            let url_str: String = match env.get_string(&url) {
                Ok(s) => s.into(),
                Err(_) => return JNI_FALSE,
            };
            let source_str: String = match env.get_string(&source_url) {
                Ok(s) => s.into(),
                Err(_) => return JNI_FALSE,
            };
            let type_str: String = match env.get_string(&request_type) {
                Ok(s) => s.into(),
                Err(_) => return JNI_FALSE,
            };

            if let Ok(request) = Request::new(&url_str, &source_str, &type_str) {
                let result = engine.check_network_request(&request);
                return if result.matched { JNI_TRUE } else { JNI_FALSE };
            }
        }

        JNI_FALSE
    }));

    match result {
        Ok(val) => val,
        Err(_) => JNI_FALSE,
    }
}

#[no_mangle]
pub extern "system" fn Java_com_adghost_app_util_NativeAdBlockEngine_nativeDestroy(
    _env: JNIEnv,
    _class: JClass,
) {
    let mut guard = ENGINE.lock().unwrap();
    *guard = None;
}
