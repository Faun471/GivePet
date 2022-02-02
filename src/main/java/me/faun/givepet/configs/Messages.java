package me.faun.givepet.configs;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.StringProperty;

public final class Messages implements SettingsHolder {
    public Messages() {

    }

    @Override
    public void registerComments(CommentsConfiguration config) {
        config.setComment("",
                " .88888.  oo                       888888ba             dP   " ,
                              "d8'   `88                          88    `8b            88   " ,
                              "88        dP dP   .dP .d8888b.    a88aaaa8P' .d8888b. d8888P " ,
                              "88   YP88 88 88   d8' 88ooood8     88        88ooood8   88   " ,
                              "Y8.   .88 88 88 .88'  88.  ...     88        88.  ...   88   " ,
                              " `88888'  dP 8888P'   `88888P'     dP        `88888P'   dP" ,
                              "\n" ,
                              " This is GivePet's messages.yml, if you wish to know how to  " ,
                              " format the messages, please refer to this site: https://mf.mattstudios.me/message/mf-msg/syntax",
                              "\n");
    }

    public static final Property<String> PREFIX = new StringProperty("prefix", "<g:#4884ee:#06bcfb>GivePet");

    public static final Property<String> PLAYER_NOT_ONLINE = new StringProperty("player-not-online", "%prefix% &fThat player is not online.");

    public static final Property<String> RELOAD_SUCCESS = new StringProperty("reload-success", "%prefix% &aSuccessfully reloaded the plugin.");

    public static final Property<String> NO_PERMISSION = new StringProperty("no-permission", "%prefix% &cYou do not have permission to run this command!");

    public static final Property<String> GIVE_PET_SUCCESS = new StringProperty("give-pet-success", "%prefix% &fSuccessfully gave %receiver% your pet!");

    public static final Property<String> RECEIVE_PET_SUCCESS = new StringProperty("receive-pet-success", "%prefix% &f%giver% gave you their pet.");

    public static final Property<String> NOT_YOUR_PET = new StringProperty("not-your-pet", "%prefix% &cThat's not your pet.");

    public static final Property<String> CANNOT_TRANSFER_SELF = new StringProperty("cannot-transfer-self", "%prefix% &cYou can't transfer a pet to yourself!");

    public static final Property<String> GIVER_REQUEST_MESSAGE = new StringProperty("giver-request-message", "%prefix% &fYou sent %receiver% a request.");

    public static final Property<String> RECEIVER_REQUEST_MESSAGE = new StringProperty("receiver-request-message", "%prefix% &f%giver% wants to give you a pet! &7[[&aAccept&7]](hover: &fClick to accept.|command:/givepet accept)  &7[[&cReject&7]](hover: &fClick to reject.|command:/givepet reject)");

    public static final Property<String> GIVER_REQUEST_EXPIRED = new StringProperty("giver-request-expired", "%prefix% &c%receiver% did not accept in time.");

    public static final Property<String> RECEIVER_REQUEST_EXPIRED = new StringProperty("receiver-request-expired", "%prefix% &cRequest expired.");

    public static final Property<String> PENDING_REQUEST = new StringProperty("pending-request", "%prefix% &cYou still have an active request with a player!");

    public static final Property<String> NO_PENDING_REQUEST = new StringProperty("no-pending-request", "%prefix% &cNo one sent you a request.");

    public static final Property<String> GIVER_REQUEST_ACCEPT = new StringProperty("sender-accept-request", "%prefix% &f%receiver% accepted the request.\nRight click the pet that you'd like to give");

    public static final Property<String> RECEIVER_REQUEST_ACCEPT = new StringProperty("receiver-accept-request", "%prefix% &fYou accepted %giver%'s request.");

    public static final Property<String> GIVER_REQUEST_REJECT = new StringProperty("sender-reject-request", "%prefix% &f%receiver% rejected the request.");

    public static final Property<String> RECEIVER_REQUEST_REJECT = new StringProperty("receiver-reject-request", "%prefix% &fYou rejected %giver%'s request.");


}
