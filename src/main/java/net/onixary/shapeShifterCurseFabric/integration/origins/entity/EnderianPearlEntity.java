package net.onixary.shapeShifterCurseFabric.integration.origins.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModEntities;

public class EnderianPearlEntity extends ThrownItemEntity {
   public EnderianPearlEntity(EntityType<? extends EnderianPearlEntity> entityType, World world) {
      super(entityType, world);
   }

   public EnderianPearlEntity(World world, LivingEntity owner) {
      super(ModEntities.ENDERIAN_PEARL, owner, world);
   }

   @Environment(EnvType.CLIENT)
   public EnderianPearlEntity(World world, double x, double y, double z) {
      super(ModEntities.ENDERIAN_PEARL, x, y, z, world);
   }

   protected Item getDefaultItem() {
      return Items.ENDER_PEARL;
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      Entity entity = this.getOwner();

      for(int i = 0; i < 32; ++i) {
         this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
      }

      if (!this.getWorld().isClient && !this.isRemoved()) {
         if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
		      if (serverPlayerEntity.networkHandler.isConnectionOpen() && serverPlayerEntity.getWorld() == this.getWorld() && !serverPlayerEntity.isSleeping()) {

               if (entity.hasVehicle()) {
                  entity.stopRiding();
               }

               entity.requestTeleport(this.getX(), this.getY(), this.getZ());
               entity.fallDistance = 0.0F;
            }
         } else if (entity != null) {
            entity.requestTeleport(this.getX(), this.getY(), this.getZ());
            entity.fallDistance = 0.0F;
         }

         this.discard();
      }

   }

   public void tick() {
      Entity entity = this.getOwner();
      if (entity instanceof PlayerEntity && !entity.isAlive()) {
         this.discard();
      } else {
         super.tick();
      }

   }

   public Entity teleportTo(TeleportTarget target) {
      Entity entity = this.getOwner();
      if (entity != null && entity.getWorld().getRegistryKey() != target.world().getRegistryKey()) {
         this.setOwner(null);
      }
      return super.teleportTo(target);
   }
}