package com.github.inzan123.mixin;

import com.github.inzan123.ChunkLastTickComponent;
import com.github.inzan123.ChunkLongComponent;
import com.github.inzan123.SimulateRandomTicks;
import com.github.inzan123.UnloadedActivity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.inzan123.MyComponents.MAGIK;
import static java.lang.Long.max;


@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Inject(at = @At("HEAD"), method = "tickChunk")
	private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {

		ServerWorld world = (ServerWorld)(Object)this;

		ChunkLongComponent lastTick = chunk.getComponent(MAGIK);

		long currentTime = world.getTimeOfDay();

		if (lastTick.getValue() != 0) { //either new chunk or hasn't been loaded since mod was installed (until now)

			long timeDifference = max(currentTime - lastTick.getValue(),0);

			if (timeDifference > 20) {

				int minY = world.getBottomY();
				int maxY = world.getTopY();

				for (int x=0; x<16;x++) {
					for (int y=minY; y<maxY;y++) {
						for (int z=0; z<16;z++) {
							BlockPos position = new BlockPos(x,y,z);
							BlockState state = chunk.getBlockState(position);
							Block block = state.getBlock();
							if (block instanceof SimulateRandomTicks) {
								SimulateRandomTicks tickSimulator = (SimulateRandomTicks) block;
								ChunkPos chunkPos = chunk.getPos();
								BlockPos notChunkBlockPos = position.add(new BlockPos(chunkPos.x*16,0,chunkPos.z*16));
								tickSimulator.simulateRandomTicks(state, world, notChunkBlockPos, world.random, timeDifference, randomTickSpeed);
							}
						}
					}
				}
			}
		}

		lastTick.setValue(currentTime);
	}

}

