package com.enliple.pudding.commons.log;

public final class Logger {
    public static final int ALL = 0;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NONE = 6;

    private static int sLogLevel = NONE;

    private static String sLogTag = "Logger";

    private static final LogController sLogController = new LogController();

    /**
     * 로그 TAG 설정
     */
    public static void setTag(String tag) {
        sLogTag = tag;
    }

    /**
     * 로그 등급 결정
     */
    public static void setLevel(int logLevel) {
        sLogLevel = logLevel;
    }

    /**
     * 파일 로그 경로 설정
     */
    public static void setFilePath(String path) {
        e(sLogTag, "setFilePath:" + path);

        sLogController.setFilePath(path);
    }

    /**
     * 파일 로그 이름 설정
     * <p>
     * default Name : Log.txt
     */
    public static void setFileName(String name) {
        e(sLogTag, "setFileName:" + name);

        sLogController.setFileName(name);
    }

    /**
     * Error
     */
    public static void e(String... message) {
        if (sLogLevel <= ERROR) {
            if (message.length > 1) {
                sLogController.e(message[0], message[1]);
            } else {
                sLogController.e(sLogTag, message[0]);
            }
        }
    }

    /**
     * Warning
     */
    public static void w(String... message) {
        if (sLogLevel <= WARN) {
            if (message.length > 1) {
                sLogController.w(message[0], message[1]);
            } else {
                sLogController.w(sLogTag, message[0]);
            }
        }
    }

    /**
     * Information
     */
    public static void i(String... message) {
        if (sLogLevel <= INFO) {
            if (message.length > 1) {
                sLogController.i(message[0], message[1]);
            } else {
                sLogController.i(sLogTag, message[0]);
            }
        }
    }

    /**
     * Debug
     */
    public static void d(String... message) {
        if (sLogLevel <= DEBUG) {
            if (message.length > 1) {
                sLogController.d(message[0], message[1]);
            } else {
                sLogController.d(sLogTag, message[0]);
            }
        }
    }


    /**
     * Verbose
     */
    public static void v(String... message) {
        if (sLogLevel <= VERBOSE) {
            if (message.length > 1) {
                sLogController.v(message[0], message[1]);
            } else {
                sLogController.v(sLogTag, message[0]);
            }
        }
    }

    /**
     * Caller 도 같이 표시 한다.
     */
    public static void c(String... message) {
        if (sLogLevel == NONE) {
            return;
        }

        if (message.length > 1) {
            sLogController.c(message[0], message[1]);
        } else {
            sLogController.c(sLogTag, message[0]);
        }
    }

    /**
     * Large Logger.
     * <p>
     * 로그가 4000자를 넘을 경우에 사용한다.
     */
    public static void l(String... message) {
        if (sLogLevel == NONE) {
            return;
        }
        if (message.length > 1) {
            sLogController.l(message[0], message[1]);
        } else {
            sLogController.l(sLogTag, message[0]);
        }
    }

    /**
     * HighLight.
     * <p>
     * 중요 로그 표시에 사용하자.
     */
    public static void h(String... message) {
        if (sLogLevel == NONE) {
            return;
        }
        if (message.length > 1) {
            sLogController.h(message[0], message[1]);
        } else {
            sLogController.h(sLogTag, message[0]);
        }
    }

    /**
     * Print StackTrace.
     * <p>
     * Exception StackTrace 정보 표시.
     */
    public static void p(String tag, Throwable t) {
        if (sLogLevel == NONE) {
            return;
        }

        sLogController.p(tag, t);
    }

    public static void p(Throwable t) {
        if (sLogLevel == NONE) {
            return;
        }

        sLogController.p(sLogTag, t);
    }

    /**
     * JSON Format
     */
    public static void json(String... message) {
        if (sLogLevel == NONE) {
            return;
        }
        if (message.length > 1) {
            sLogController.json(message[0], message[1]);
        } else {
            sLogController.json(sLogTag, message[0]);
        }
    }

    /**
     * JSON Format
     */
    public static void json2(String... message) {
        if (sLogLevel == NONE) {
            return;
        }
        if (message.length > 1) {
            sLogController.json2(message[0], message[1]);
        } else {
            sLogController.json2(sLogTag, message[0]);
        }
    }

    /**
     * 파일에 로그를 저장 한다.  (trace 에 로그도 같이 나온다.)
     */
    public static void s(String... message) {
        if (sLogLevel == NONE) {
            return;
        }
        if (message.length > 1) {
            sLogController.s(message[0], message[1]);
        } else {
            sLogController.s(sLogTag, message[0]);
        }
    }
}