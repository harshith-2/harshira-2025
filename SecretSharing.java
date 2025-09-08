import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SecretSharing {

    static class Point {
        BigInteger x, y;
        Point(BigInteger x, BigInteger y) { this.x = x; this.y = y; }
    }

    private static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    private static BigInteger lagrangeInterpolation(List<Point> points, int k) {
        BigInteger numeratorProduct, denominatorProduct;
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            numeratorProduct = BigInteger.ONE;
            denominatorProduct = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    numeratorProduct = numeratorProduct.multiply(xj.negate());
                    denominatorProduct = denominatorProduct.multiply(xi.subtract(xj));
                }
            }

            // yi * (Π(-xj) / Π(xi - xj))
            BigInteger term = yi.multiply(numeratorProduct).divide(denominatorProduct);
            secret = secret.add(term);
        }

        return secret;
    }

    public static BigInteger solveSecret(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JSONObject obj = new JSONObject(new JSONTokener(reader));
            int k = obj.getJSONObject("keys").getInt("k");

            List<Point> points = new ArrayList<>();

            for (String key : obj.keySet()) {
                if (key.equals("keys")) continue;
                BigInteger x = new BigInteger(key);
                JSONObject entry = obj.getJSONObject(key);

                int base;
                try {
                    base = entry.getInt("base"); // if number
                } catch (Exception e) {
                    base = Integer.parseInt(entry.getString("base")); // if string
                }

                BigInteger y = decodeValue(entry.getString("value"), base);
                points.add(new Point(x, y));
            }

            // Use first k points for interpolation
            return lagrangeInterpolation(points.subList(0, k), k);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigInteger.valueOf(-1);
    }

    public static void main(String[] args) {
        String testcase1 = "testcase1.json";
        String testcase2 = "testcase2.json";

        System.out.println("Secret from testcase1: " + solveSecret(testcase1));
        System.out.println("Secret from testcase2: " + solveSecret(testcase2));
    }
}
