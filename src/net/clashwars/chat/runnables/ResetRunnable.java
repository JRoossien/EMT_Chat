package net.clashwars.chat.runnables;

import net.clashwars.chat.EMTChat;

public class ResetRunnable implements Runnable {
	
	private EMTChat chat;
    
    public ResetRunnable(EMTChat chat) {
        this.chat = chat;
    }

    @Override
    public synchronized void run() {
    	chat.resetShouts();
    	chat.setLastReset(System.currentTimeMillis());
    	chat.broadcast("&8[&6Chat&8] &aAll shout limits have been reset!");
    }
}
