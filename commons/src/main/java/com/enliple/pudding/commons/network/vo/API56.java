package com.enliple.pudding.commons.network.vo;

/**
 * Created by Kim Joonsung on 2018-12-27.
 */
public class API56 {

    /**
     * result : success
     * nTotalCount : 1
     * data : {"owner":"대표자명","name":"회사명","company_no":"123-45-67890","tel":"02-123-4567","fax":"02-123-4568","email":"정보책임자 E-mail","communication_no":"제 OO구 - 123호","addr":"OO도 OO시 OO구 OO동 123-45","hosting":"주식회사 인라이플"}
     */

    public String result;
    public int nTotalCount;
    public CompanyInfo data;

    public static class CompanyInfo {
        /**
         * owner : 대표자명
         * name : 회사명
         * company_no : 123-45-67890
         * tel : 02-123-4567
         * fax : 02-123-4568
         * email : 정보책임자 E-mail
         * communication_no : 제 OO구 - 123호
         * addr : OO도 OO시 OO구 OO동 123-45
         * hosting : 주식회사 인라이플
         */

        public String owner;
        public String name;
        public String company_no;
        public String tel;
        public String fax;
        public String email;
        public String communication_no;
        public String addr;
        public String hosting;
    }
}
