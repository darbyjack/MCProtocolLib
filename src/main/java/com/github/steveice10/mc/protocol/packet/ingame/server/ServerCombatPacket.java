package com.github.steveice10.mc.protocol.packet.ingame.server;

import com.github.steveice10.mc.protocol.data.DefaultComponentSerializer;
import com.github.steveice10.mc.protocol.data.MagicValues;
import com.github.steveice10.mc.protocol.data.game.entity.player.CombatState;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;
import net.kyori.adventure.text.Component;

import java.io.IOException;

@Data
@With
@Setter(AccessLevel.NONE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerCombatPacket implements Packet {
    private CombatState combatState;

    private int entityId;
    private int duration;
    private int playerId;
    private Component message;

    public ServerCombatPacket() {
        this.combatState = CombatState.ENTER_COMBAT;
    }

    public ServerCombatPacket(int entityId, int duration) {
        this.combatState = CombatState.END_COMBAT;

        this.entityId = entityId;
        this.duration = duration;
    }

    public ServerCombatPacket(int entityId, int playerId, @NonNull Component message) {
        this.combatState = CombatState.ENTITY_DEAD;

        this.entityId = entityId;
        this.playerId = playerId;
        this.message = message;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.combatState = MagicValues.key(CombatState.class, in.readVarInt());
        if(this.combatState == CombatState.END_COMBAT) {
            this.duration = in.readVarInt();
            this.entityId = in.readInt();
        } else if(this.combatState == CombatState.ENTITY_DEAD) {
            this.playerId = in.readVarInt();
            this.entityId = in.readInt();
            this.message = DefaultComponentSerializer.get().deserialize(in.readString());
        }
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.combatState));
        if(this.combatState == CombatState.END_COMBAT) {
            out.writeVarInt(this.duration);
            out.writeInt(this.entityId);
        } else if(this.combatState == CombatState.ENTITY_DEAD) {
            out.writeVarInt(this.playerId);
            out.writeInt(this.entityId);
            out.writeString(DefaultComponentSerializer.get().serialize(this.message));
        }
    }

    @Override
    public boolean isPriority() {
        return false;
    }
}
