package cn.haier.bio.medical.hpce;

import java.math.BigDecimal;

public class TemperatureTools {
    /**
     * 显示摄氏度转设定摄氏度
     *
     * @param celsius
     * @return
     */
    public static int uc2SC(float celsius) {
        return new BigDecimal(celsius * 100)
                .setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    /**
     * 显示华氏度转设定摄氏度
     *
     * @param fahrenheit
     * @return
     */
    public static int uf2SC(float fahrenheit) {
        double celsius = (fahrenheit - 32) * f2cScale();
        return new BigDecimal(celsius * 100)
                .setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    /**
     * 设定摄氏度转显示摄氏度
     *
     * @param celsius
     * @return
     */
    public static float sc2UC(int celsius) {
        return new BigDecimal(celsius / 100.0)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .floatValue();
    }

    /**
     * 设定摄氏度转显示华氏度
     *
     * @param celsius
     * @return
     */
    public static float sc2UF(int celsius) {
        double fahrenheit = 32 + celsius * c2fScale() / 100.0;
        return new BigDecimal(fahrenheit)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .floatValue();
    }

    /**
     * 主控板摄氏度转显示摄氏度
     *
     * @param celsius
     * @return
     */
    public static float mc2UC(int celsius) {
        return new BigDecimal(celsius / 100.0 - 300)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .floatValue();
    }

    /**
     * 设定摄氏度转主控板摄氏度
     *
     * @param celsius
     * @return
     */
    public static float mc2UF(int celsius) {
        double fahrenheit = 32 + (celsius / 100.0 - 300) * c2fScale();
        return new BigDecimal(fahrenheit)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .floatValue();
    }


    /**
     * 摄氏度转华氏度系数
     *
     * @return
     */
    private static double c2fScale() {
        return 9 / 5.0;
    }

    /**
     * 华氏度转摄氏度系数
     *
     * @return
     */
    private static double f2cScale() {
        return 5 / 9.0;
    }
}
