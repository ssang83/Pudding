package com.enliple.pudding.commons.app;

public class VersionCompare {
    /**
     * 앱 버전 비교
     *
     * @param v1 현재 앱 버전 - 매니페스트 버전 이름
     * @param v2 서버에서 받은 앱 버전
     * @return <p>
     * -2(INVALID_FORMAT_RESULT) - (invalid format 에러)
     * </p>
     * <p>
     * -1(COMPARE_BIG_PREV_PARAM) - (현재 버전이 더 높다.)
     * </p>
     * <p>
     * 0(COMPARE_EQUALS_PARAM) - (현재 버전과 서버의 버전이 서로 같다.)
     * </p>
     * <p>
     * 1(COMPARE_BIG_AFTER_PARAM) - (현재 버전이 더 낮다.)
     * </p>
     */
    public static final int compareVersions(String v1, String v2) {
        if (v1 == null || v2 == null || v1.trim().equals("") || v2.trim().equals("")) {
            return -2;
        } else if (v1.equals(v2)) {
            return 0;
        } else {
            boolean valid1 = v1.matches("\\d+\\.\\d+\\.\\d+");
            boolean valid2 = v2.matches("\\d+\\.\\d+\\.\\d+");

            if (valid1 && valid2) {
                int[] nums1 = null;
                int[] nums2 = null;

                try {
                    nums1 = StringUtils.Companion.convertStringArrayToIntArray(v1.split("\\."));
                    nums2 = StringUtils.Companion.convertStringArrayToIntArray(v2.split("\\."));
                } catch (NumberFormatException e) {
                    return -2;
                } catch (Exception e) {
                    return -2;
                }

                if (nums1.length == 3 && nums2.length == 3) {
                    if (nums1[0] < nums2[0]) {
                        return 1;
                    } else if (nums1[0] > nums2[0]) {
                        return -1;
                    } else {
                        if (nums1[1] < nums2[1]) {
                            return 1;
                        } else if (nums1[1] > nums2[1]) {
                            return -1;
                        } else {
                            if (nums1[2] < nums2[2]) {
                                return 1;
                            } else if (nums1[2] > nums2[2]) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }
                } else {
                    return -2;
                }
            } else {
                return -2;
            }
        }
    }
}
