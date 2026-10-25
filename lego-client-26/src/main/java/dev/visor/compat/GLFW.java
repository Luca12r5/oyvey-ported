// Minecraft 26.3 stand-in for org.lwjgl.glfw.GLFW in the Visor sources (26.3 reads input through SDL3).
package dev.visor.compat;

/**
 * GLFW key, modifier and action values. The Visor logic keeps working with GLFW codes on 26.3:
 * the input mixins translate the SDL codes of the game to these (see dev.lego.compat.Keys) before
 * they reach the Visor, so ranges like A..Z and 0..9 stay contiguous as the code expects.
 */
public final class GLFW {
   public static final int GLFW_RELEASE = 0;
   public static final int GLFW_PRESS = 1;
   public static final int GLFW_REPEAT = 2;

   public static final int GLFW_MOD_SHIFT = 0x0001;
   public static final int GLFW_MOD_CONTROL = 0x0002;
   public static final int GLFW_MOD_ALT = 0x0004;
   public static final int GLFW_MOD_SUPER = 0x0008;

   public static final int GLFW_KEY_UNKNOWN = -1;
   public static final int GLFW_KEY_SPACE = 32;
   public static final int GLFW_KEY_APOSTROPHE = 39;
   public static final int GLFW_KEY_COMMA = 44;
   public static final int GLFW_KEY_MINUS = 45;
   public static final int GLFW_KEY_PERIOD = 46;
   public static final int GLFW_KEY_SLASH = 47;
   public static final int GLFW_KEY_0 = 48;
   public static final int GLFW_KEY_1 = 49;
   public static final int GLFW_KEY_2 = 50;
   public static final int GLFW_KEY_3 = 51;
   public static final int GLFW_KEY_4 = 52;
   public static final int GLFW_KEY_5 = 53;
   public static final int GLFW_KEY_6 = 54;
   public static final int GLFW_KEY_7 = 55;
   public static final int GLFW_KEY_8 = 56;
   public static final int GLFW_KEY_9 = 57;
   public static final int GLFW_KEY_SEMICOLON = 59;
   public static final int GLFW_KEY_EQUAL = 61;
   public static final int GLFW_KEY_A = 65;
   public static final int GLFW_KEY_B = 66;
   public static final int GLFW_KEY_C = 67;
   public static final int GLFW_KEY_D = 68;
   public static final int GLFW_KEY_E = 69;
   public static final int GLFW_KEY_F = 70;
   public static final int GLFW_KEY_G = 71;
   public static final int GLFW_KEY_H = 72;
   public static final int GLFW_KEY_I = 73;
   public static final int GLFW_KEY_J = 74;
   public static final int GLFW_KEY_K = 75;
   public static final int GLFW_KEY_L = 76;
   public static final int GLFW_KEY_M = 77;
   public static final int GLFW_KEY_N = 78;
   public static final int GLFW_KEY_O = 79;
   public static final int GLFW_KEY_P = 80;
   public static final int GLFW_KEY_Q = 81;
   public static final int GLFW_KEY_R = 82;
   public static final int GLFW_KEY_S = 83;
   public static final int GLFW_KEY_T = 84;
   public static final int GLFW_KEY_U = 85;
   public static final int GLFW_KEY_V = 86;
   public static final int GLFW_KEY_W = 87;
   public static final int GLFW_KEY_X = 88;
   public static final int GLFW_KEY_Y = 89;
   public static final int GLFW_KEY_Z = 90;
   public static final int GLFW_KEY_LEFT_BRACKET = 91;
   public static final int GLFW_KEY_BACKSLASH = 92;
   public static final int GLFW_KEY_RIGHT_BRACKET = 93;
   public static final int GLFW_KEY_GRAVE_ACCENT = 96;
   public static final int GLFW_KEY_ESCAPE = 256;
   public static final int GLFW_KEY_ENTER = 257;
   public static final int GLFW_KEY_TAB = 258;
   public static final int GLFW_KEY_BACKSPACE = 259;
   public static final int GLFW_KEY_INSERT = 260;
   public static final int GLFW_KEY_DELETE = 261;
   public static final int GLFW_KEY_RIGHT = 262;
   public static final int GLFW_KEY_LEFT = 263;
   public static final int GLFW_KEY_DOWN = 264;
   public static final int GLFW_KEY_UP = 265;
   public static final int GLFW_KEY_PAGE_UP = 266;
   public static final int GLFW_KEY_PAGE_DOWN = 267;
   public static final int GLFW_KEY_HOME = 268;
   public static final int GLFW_KEY_END = 269;
   public static final int GLFW_KEY_CAPS_LOCK = 280;
   public static final int GLFW_KEY_F1 = 290;
   public static final int GLFW_KEY_F2 = 291;
   public static final int GLFW_KEY_F3 = 292;
   public static final int GLFW_KEY_F4 = 293;
   public static final int GLFW_KEY_F5 = 294;
   public static final int GLFW_KEY_F11 = 300;
   public static final int GLFW_KEY_F12 = 301;
   public static final int GLFW_KEY_KP_ENTER = 335;
   public static final int GLFW_KEY_LEFT_SHIFT = 340;
   public static final int GLFW_KEY_LEFT_CONTROL = 341;
   public static final int GLFW_KEY_LEFT_ALT = 342;
   public static final int GLFW_KEY_LEFT_SUPER = 343;
   public static final int GLFW_KEY_RIGHT_SHIFT = 344;
   public static final int GLFW_KEY_RIGHT_CONTROL = 345;
   public static final int GLFW_KEY_RIGHT_ALT = 346;
   public static final int GLFW_KEY_RIGHT_SUPER = 347;

   private GLFW() {
   }
}
