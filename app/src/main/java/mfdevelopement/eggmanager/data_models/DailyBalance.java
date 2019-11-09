package mfdevelopement.eggmanager.data_models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "dailyBalanceTable")
public class DailyBalance implements Serializable {

    public static final String COL_DATE_PRIMARY_KEY = "dateKey";
    public static final String COL_EGGS_COLLECTED_NAME = "eggsFetched";
    public static final String COL_EGGS_SOLD_NAME = "eggsSold";
    private static final String COL_PRICE_PER_EGG = "pricePerEgg";
    private static final String COL_NUMBER_HENS = "numberOfHens";
    public static final String COL_MONEY_EARNED = "moneyEarned";
    public static final String dateKeyFormat = "yyyyMMdd";
    public static final int NOT_SET = 0;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = COL_DATE_PRIMARY_KEY)
    private String dateKey;

    @ColumnInfo(name = COL_EGGS_COLLECTED_NAME)
    private int eggsCollected;

    @ColumnInfo(name = COL_EGGS_SOLD_NAME)
    private int eggsSold;

    @ColumnInfo(name = COL_PRICE_PER_EGG)
    private double pricePerEgg;

    @ColumnInfo(name = COL_MONEY_EARNED)
    private double moneyEarned;

    @ColumnInfo(name = COL_NUMBER_HENS)
    private int numHens;

    public DailyBalance(@NonNull String dateKey, int eggsCollected, int eggsSold, double pricePerEgg) {
        this.dateKey = dateKey;
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        this.numHens = 0;
        this.moneyEarned = calcMoneyEarned(eggsSold,pricePerEgg);
    }

    private String getDateKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateKeyFormat, Locale.getDefault());
        return sdf.format(date);
    }

    private double calcMoneyEarned(int eggsSold, double pricePerEgg) {
        // calculate the earned money
        if (eggsSold != 0 && pricePerEgg != 0)
            return eggsSold*pricePerEgg;
        else
            return 0;
    }

    @NonNull
    public String getDateKey(){
        return this.dateKey;
    }

    public int getEggsCollected() { return  this.eggsCollected; }

    public int getEggsSold() { return this.eggsSold; }

    public double getPricePerEgg() { return this.pricePerEgg; }

    public Date getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateKeyFormat, Locale.getDefault());
        Date date;

        try {
            date = sdf.parse(this.dateKey);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public int getNumHens() { return this.numHens; }

    public void setNumHens(int numHens) {
        this.numHens = numHens;
    }

    public double getMoneyEarned() { return this.moneyEarned; }

    public void setMoneyEarned(double money) {
        this.moneyEarned = money;
    }
}
