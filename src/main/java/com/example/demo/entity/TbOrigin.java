package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * 
 * </p>
 *
 * @author feiyyu
 * @since 2022-06-14
 */
//@EqualsAndHashCode(of = {"date", "coin", "direction"})
@TableName("tb_origin")
public class TbOrigin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String date;

    private String direction;

    private String coin;

    private Double transactionAmount;

    private Double avgPrice;

    private Double profit;

    private String requestId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    /**
     * @return String return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "TbOrigin{" +
                "id=" + id +
                ", date=" + date +
                ", direction=" + direction +
                ", coin=" + coin +
                ", transactionAmount=" + transactionAmount +
                ", avgPrice=" + avgPrice +
                ", profit=" + profit +
                "}";
    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        TbOrigin myObject = (TbOrigin) o;
//        return Objects.equals(date, myObject.getDate()) && Objects.equals(coin, myObject.getCoin());
//    }
//
//    @Override
//    public int hashCode() {
//        int result = 17;
//        result = 31 * result + (date == null ? 0 : date.hashCode());
//        result = 31 * result + (coin == null ? 0 : coin.hashCode());
//        return result;
//    }

}
