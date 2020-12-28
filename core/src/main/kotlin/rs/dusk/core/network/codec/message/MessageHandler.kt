package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session

abstract class MessageHandler<M : Message> {

    /**
     * Handles what to do with message [M]
     */
    abstract fun handle(ctx: ChannelHandlerContext, msg: M)

    open fun handleCommand(session: Session, command: String) {}

    open fun handleDialogue(session: Session, hash: Int, button: Int) {}

    open fun handleFloorItem(session: Session, id: Int, run: Boolean, y: Int, x: Int, option: Int) {}

    open fun handleGameLogin(
        ctx: ChannelHandlerContext, username: String,
        password: String,
        isaacKeys: IntArray,
        mode: Int,
        width: Int,
        height: Int,
        antialias: Int,
        settings: String,
        affiliate: Int,
        session: Int,
        os: Int,
        is64Bit: Int,
        versionType: Int,
        vendorType: Int,
        javaRelease: Int,
        javaVersion: Int,
        javaUpdate: Int,
        isUnsigned: Int,
        heapSize: Int,
        processorCount: Int,
        totalMemory: Int
    ) {
    }

    open fun handleIntEntry(session: Session, integer: Int) {}

    open fun handleInterfaceClose(session: Session) {}

    open fun handleInterfaceOption(session: Session, hash: Int, itemId: Int, itemSlot: Int, option: Int) {}

    open fun handleInterfaceSwitch(
        session: Session,
        toType: Int,
        fromSlot: Int,
        fromType: Int,
        fromHash: Int,
        toSlot: Int,
        toHash: Int
    ) {
    }

    open fun handleNPCOption(
        session: Session,
        run: Boolean,
        npcIndex: Int,
        option: Int
    ) {
    }

    open fun handleObjectOption(
        session: Session,
        objectId: Int,
        x: Int,
        y: Int,
        run: Boolean,
        option: Int
    ) {
    }

    open fun handlePlayerOption(session: Session, index: Int, option: Int) {}

    open fun handleRegionLoaded(session: Session) {}

    open fun handleScreenChange(
        session: Session,
        displayMode: Int,
        width: Int,
        height: Int,
        antialiasLevel: Int
    ) {
    }

    open fun handleStringEntry(session: Session, text: String) {}

    open fun handleWalkMap(session: Session, x: Int, y: Int, running: Boolean) {}

    open fun handleWalkMiniMap(session: Session, x: Int, y: Int, running: Boolean) {}

}