package com.yuntan.infra.client.map.ip.dto;

import lombok.Data;

@Data
public class AmapBaseResponse {

    /** 1 成功 0 失败 */
    private String status;
    /** 状态信息 */
    private String info;
    /** 状态码信息 */
    private String infocode;

}
