package net.minecraft.world.level.gameevent;

import net.minecraft.server.level.ServerLevel;

public interface GameEventListener {
   default boolean handleEventsImmediately() {
      return false;
   }

   PositionSource getListenerSource();

   int getListenerRadius();

   boolean handleGameEvent(ServerLevel p_223757_, GameEvent.Message p_223758_);
}