package id.deathrealm;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        ChunkData chunk = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biome.setBiome(x, z, Biome.THE_VOID);
            }
        }

        return chunk;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}
