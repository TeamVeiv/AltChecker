package ch.darknight;

import java.net.InetAddress;
import java.net.Proxy;
import java.util.UUID;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.Session;

public class Check {

	protected static MinecraftSessionService service;

	public static Session login(String username, String password, Proxy proxy) {
		YggdrasilUserAuthentication auth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString()), Agent.MINECRAFT);
		auth.setUsername(username);
		auth.setPassword(password);
		try {
			auth.logIn();
			return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
		} catch (AuthenticationException e) {
			System.out.println("Login Failed");
		}
		return null;
	}

	public static boolean connect(final String ip, final int port, Session session, Proxy proxy) {
		if (Check.service == null) {
			Check.service = (new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString())).createMinecraftSessionService();
		}
		try {
			InetAddress var1 = InetAddress.getByName(ip);
			NetworkManager networkManager = NetworkManager.provideLanClient(var1, port);
			networkManager.setNetHandler(new NetHandlerLoginClient_Modded(networkManager, session, Check.service));
			networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));
			networkManager.sendPacket(new C00PacketLoginStart(session.getProfile()));
			while (((NetHandlerLoginClient_Modded) networkManager.getNetHandler()).isConnecting()) {
				Thread.sleep(5000);
			}
			return ((NetHandlerLoginClient_Modded) networkManager.getNetHandler()).isConnected();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
