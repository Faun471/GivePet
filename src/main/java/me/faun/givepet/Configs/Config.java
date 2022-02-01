package me.faun.givepet.Configs;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.BooleanProperty;
import ch.jalu.configme.properties.IntegerProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.StringProperty;

public final class Config implements SettingsHolder {
    public Config() {

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
                " This is GivePet's config.yml, if you wish to know how to  " ,
                " format the messages, please refer to this site: https://mf.mattstudios.me/message/mf-msg/syntax",
                "\n");
    }

    @Comment({"\n","Should the pet stand up when their owner changes?", "Default Value: true"})
    public static final Property<Boolean> PET_STAND = new BooleanProperty("stand-when-transferred", true);

    @Comment({"\n","Should the pet teleport to their new owner?", "Default Value: false"})
    public static final Property<Boolean> PET_TELEPORT = new BooleanProperty("teleport-when-transferred", false);

    @Comment({"\n","How much time in seconds should the pet be invincible upon owner change?", "Default Value: 15"})
    public static final Property<Integer> PET_INVINCIBILITY = new IntegerProperty("invincibility-on-transfer", 15);
}
