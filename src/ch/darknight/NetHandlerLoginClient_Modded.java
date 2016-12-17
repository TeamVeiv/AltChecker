package ch.darknight;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.concurrent.SynchronousQueue;

import javax.crypto.SecretKey;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.CryptManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;

public class NetHandlerLoginClient_Modded implements INetHandlerLoginClient
{
    private final Session field_147394_b;
    private final NetworkManager field_147393_d;
    private MinecraftSessionService service;
	private boolean connected;
	private boolean connecting = true;

    public NetHandlerLoginClient_Modded(NetworkManager p_i45059_1_, Session session, MinecraftSessionService minecraftSessionService)
    {
        this.field_147393_d = p_i45059_1_;
        this.field_147394_b = session;
        this.service = minecraftSessionService;
    }

    public void handleEncryptionRequest(S01PacketEncryptionRequest packetIn)
    {
        final SecretKey var2 = CryptManager.createNewSharedKey();
        String var3 = packetIn.func_149609_c();
        PublicKey var4 = packetIn.func_149608_d();
        String var5 = (new BigInteger(CryptManager.getServerIdHash(var3, var4, var2))).toString(16);
        try
        {
            this.func_147391_c().joinServer(this.field_147394_b.getProfile(), this.field_147394_b.getToken(), var5);
        }
        catch (AuthenticationUnavailableException var7)
        {
            this.field_147393_d.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new Object[] {new ChatComponentTranslation("disconnect.loginFailedInfo.serversUnavailable", new Object[0])}));
            setConnecting(false);
            return;
        }
        catch (InvalidCredentialsException var8)
        {
            this.field_147393_d.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new Object[] {new ChatComponentTranslation("disconnect.loginFailedInfo.invalidSession", new Object[0])}));
            setConnecting(false);
            return;
        }
        catch (AuthenticationException var9)
        {
            this.field_147393_d.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new Object[] {var9.getMessage()}));
            setConnecting(false);
            return;
        }

        this.field_147393_d.sendPacket(new C01PacketEncryptionResponse(var2, var4, packetIn.func_149607_e()), new GenericFutureListener()
        {
            public void operationComplete(Future p_operationComplete_1_)
            {
                NetHandlerLoginClient_Modded.this.field_147393_d.enableEncryption(var2);
            }
        }, new GenericFutureListener[0]);
    }

    private MinecraftSessionService func_147391_c()
    {
        return this.service;
    }

    public void handleLoginSuccess(S02PacketLoginSuccess packetIn)
    {
    	this.setConnected(true);
        this.field_147393_d.closeChannel(new ChatComponentText("Quitting"));
        setConnecting(false);
    }

    private void setConnected(boolean b) {
		this.connected = b;
	}
    public boolean isConnected() {
		return this.connected;
	}
	/**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason)
    {
    	setConnecting(false);
    }

    public void handleDisconnect(S00PacketDisconnect packetIn)
    {
        this.field_147393_d.closeChannel(packetIn.func_149603_c());
        setConnecting(false);
    }

    public void func_180464_a(S03PacketEnableCompression p_180464_1_)
    {
        if (!this.field_147393_d.isLocalChannel())
        {
            this.field_147393_d.setCompressionTreshold(p_180464_1_.func_179731_a());
        }
    }
    public void setConnecting(boolean b) {
		this.connecting = b;
	}
	public boolean isConnecting() {
		return this.connecting;
	}
}
