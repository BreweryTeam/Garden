package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.Encoder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlantPdcType implements PersistentDataType<byte[], GardenPlant> {

    private static final PlantPdcType PLANT_TYPE = new PlantPdcType();
    public static final ListPersistentDataType<byte[], GardenPlant> PLANT_LIST_TYPE = PersistentDataType.LIST.listTypeFrom(PLANT_TYPE);
    public static final NamespacedKey PLANT_KEY = Garden.key("plant");

    private static final byte VERSION = 0;

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<GardenPlant> getComplexType() {
        return GardenPlant.class;
    }

    @Override
    public @NonNull byte[] toPrimitive(@NonNull GardenPlant complex, @NotNull PersistentDataAdapterContext context) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeByte(VERSION);
            PlantStructure structure = complex.getStructure();
            Location origin = structure.origin();
            dataOutputStream.write(Encoder.asBytes(complex.getId()));
            dataOutputStream.write(Encoder.asBytes(origin.getWorld().getUID()));
            dataOutputStream.writeInt(origin.getBlockX());
            dataOutputStream.writeInt(origin.getBlockY());
            dataOutputStream.writeInt(origin.getBlockZ());
            dataOutputStream.writeInt(Encoder.asInteger(structure.transformation()));
            dataOutputStream.writeUTF(Garden.minimized(complex.getType().key()));
            dataOutputStream.writeUTF(complex.getTrack());
            dataOutputStream.writeInt(complex.getAge());
            dataOutputStream.writeInt(complex.getExpectedFruits());
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public @NonNull GardenPlant fromPrimitive(@NonNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(primitive); DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            byte version = dataInputStream.readByte();
            if (version != VERSION) {
                throw new IllegalArgumentException("Unsupported version");
            }
            UUID id = Encoder.asUuid(dataInputStream.readNBytes(16));
            UUID worldUuid = Encoder.asUuid(dataInputStream.readNBytes(16));
            World world = Bukkit.getWorld(worldUuid);
            if (world == null) {
                throw new IllegalArgumentException("Unknown world!");
            }
            int x = dataInputStream.readInt();
            int y = dataInputStream.readInt();
            int z = dataInputStream.readInt();
            Location origin = new Location(world, x, y, z);
            Matrix3d transformation = Encoder.asTransformation(dataInputStream.readInt());
            NamespacedKey type = Garden.key(dataInputStream.readUTF());
            PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(type);
            if (plantType == null) {
                throw new IllegalArgumentException("Removed plant type");
            }
            String track = dataInputStream.readUTF();
            int age = dataInputStream.readInt();
            int fruits = dataInputStream.readInt();
            PlantStructure structure = plantType.getStructure(origin, age, track, transformation)
                    .orElseThrow(() -> new IllegalArgumentException("Can't find matching structure"));
            return new GardenPlant(id, plantType, structure, track, age, fruits, StorageSolution.PDC);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
