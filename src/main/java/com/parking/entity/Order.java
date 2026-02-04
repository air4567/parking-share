package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long parkingSpotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer hours;
    private BigDecimal pricePerHour;
    private BigDecimal totalPrice;
    /** 状态：pending-待支付 ongoing-进行中 completed-已完成 cancelled-已取消 */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
