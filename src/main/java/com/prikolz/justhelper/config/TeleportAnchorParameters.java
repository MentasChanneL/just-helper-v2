package com.prikolz.justhelper.config;

public class TeleportAnchorParameters extends ConfigObject {
    public BooleanParameter enabled = boolParameter("enable", true);
    public BooleanParameter sendMessage = boolParameter("send_message", true);
    public StringParameter icon = stringParameter("icon", "<#AAFF00><shadow:#000000FF>⚓");
    public BooleanParameter marker = boolParameter("3d_marker", true);
}
