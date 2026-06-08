package com.gsb.logistics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item")
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物品名称 */
    @Column(nullable = false, length = 128)
    private String name;

    /** 物品编码 */
    @Column(unique = true, length = 64)
    private String code;

    /** 分类 ID */
    @Column(name = "category_id")
    private Long categoryId;

    /** 存放位置 ID */
    @Column(name = "location_id")
    private Long locationId;

    /** 总数量 */
    @Column(nullable = false)
    private Integer totalQuantity = 0;

    /** 当前可借数量 */
    @Column(nullable = false)
    private Integer availableQuantity = 0;

    /** 维修中数量 */
    @Column(nullable = false)
    private Integer repairingQuantity = 0;

    /** 责任人 */
    @Column(length = 64)
    private String owner;

    /** 备注 */
    @Column(length = 500)
    private String remark;
}
