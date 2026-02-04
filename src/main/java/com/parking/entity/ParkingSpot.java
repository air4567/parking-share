package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("parking_spot")
public class ParkingSpot {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long communityId;
    private Long ownerId;
    private String spotNumber;
    /** 状态：available-可用 occupied-已占用 reserved-已预订 */
    private String status;
    private BigDecimal pricePerHour;
    private String description;
    /** 图片URL，JSON数组 */
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
