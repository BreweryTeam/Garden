package dev.jsinco.brewery.garden.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.joml.Matrix3d;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class Encoder {

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static String serializeTransformation(Matrix3d matrix3d) {
        double[] doubles = matrix3d.get(new double[9]);
        JsonArray output = new JsonArray();
        for (double aDouble : doubles) {
            output.add(aDouble);
        }
        return output.toString();
    }

    public static Matrix3d deserializeTransformation(String matrixString) {
        JsonArray jsonElement = JsonParser.parseString(matrixString).getAsJsonArray();
        List<JsonElement> jsonElementList = jsonElement.asList();
        double[] m = new double[9];
        for (int i = 0; i < jsonElementList.size(); i++) {
            m[i] = jsonElementList.get(i).getAsDouble();
        }
        return new Matrix3d(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
    }
}
