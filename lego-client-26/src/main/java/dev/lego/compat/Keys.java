package dev.lego.compat;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * 26.3 reads the keyboard through SDL3, so its key codes are no longer GLFW codes. The shared
 * client code (default key mappings, module keys in the config, GUI key handling) keeps using
 * GLFW codes, the same on both game versions; this table translates at the edge.
 */
public final class Keys {
   private static final Map<Integer, Integer> TO_GAME = new HashMap<>();
   private static final Map<Integer, Integer> TO_GLFW = new HashMap<>();

   static {
      int[] letters = {
         InputConstants.KEY_A, InputConstants.KEY_B, InputConstants.KEY_C, InputConstants.KEY_D, InputConstants.KEY_E,
         InputConstants.KEY_F, InputConstants.KEY_G, InputConstants.KEY_H, InputConstants.KEY_I, InputConstants.KEY_J,
         InputConstants.KEY_K, InputConstants.KEY_L, InputConstants.KEY_M, InputConstants.KEY_N, InputConstants.KEY_O,
         InputConstants.KEY_P, InputConstants.KEY_Q, InputConstants.KEY_R, InputConstants.KEY_S, InputConstants.KEY_T,
         InputConstants.KEY_U, InputConstants.KEY_V, InputConstants.KEY_W, InputConstants.KEY_X, InputConstants.KEY_Y,
         InputConstants.KEY_Z
      };
      for (int i = 0; i < letters.length; i++) put(65 + i, letters[i]);
      int[] digits = {
         InputConstants.KEY_0, InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3, InputConstants.KEY_4,
         InputConstants.KEY_5, InputConstants.KEY_6, InputConstants.KEY_7, InputConstants.KEY_8, InputConstants.KEY_9
      };
      for (int i = 0; i < digits.length; i++) put(48 + i, digits[i]);
      int[] fkeys = {
         InputConstants.KEY_F1, InputConstants.KEY_F2, InputConstants.KEY_F3, InputConstants.KEY_F4, InputConstants.KEY_F5,
         InputConstants.KEY_F6, InputConstants.KEY_F7, InputConstants.KEY_F8, InputConstants.KEY_F9, InputConstants.KEY_F10,
         InputConstants.KEY_F11, InputConstants.KEY_F12, InputConstants.KEY_F13, InputConstants.KEY_F14, InputConstants.KEY_F15,
         InputConstants.KEY_F16, InputConstants.KEY_F17, InputConstants.KEY_F18, InputConstants.KEY_F19, InputConstants.KEY_F20,
         InputConstants.KEY_F21, InputConstants.KEY_F22, InputConstants.KEY_F23, InputConstants.KEY_F24
      };
      for (int i = 0; i < fkeys.length; i++) put(290 + i, fkeys[i]);
      int[] numpad = {
         InputConstants.KEY_NUMPAD0, InputConstants.KEY_NUMPAD1, InputConstants.KEY_NUMPAD2, InputConstants.KEY_NUMPAD3,
         InputConstants.KEY_NUMPAD4, InputConstants.KEY_NUMPAD5, InputConstants.KEY_NUMPAD6, InputConstants.KEY_NUMPAD7,
         InputConstants.KEY_NUMPAD8, InputConstants.KEY_NUMPAD9
      };
      for (int i = 0; i < numpad.length; i++) put(320 + i, numpad[i]);
      put(330, InputConstants.KEY_NUMPADCOMMA);
      put(332, InputConstants.KEY_MULTIPLY);
      put(334, InputConstants.KEY_ADD);
      put(335, InputConstants.KEY_NUMPADENTER);
      put(336, InputConstants.KEY_NUMPADEQUALS);
      put(32, InputConstants.KEY_SPACE);
      put(39, InputConstants.KEY_APOSTROPHE);
      put(44, InputConstants.KEY_COMMA);
      put(45, InputConstants.KEY_MINUS);
      put(46, InputConstants.KEY_PERIOD);
      put(47, InputConstants.KEY_SLASH);
      put(59, InputConstants.KEY_SEMICOLON);
      put(61, InputConstants.KEY_EQUALS);
      put(91, InputConstants.KEY_LBRACKET);
      put(92, InputConstants.KEY_BACKSLASH);
      put(93, InputConstants.KEY_RBRACKET);
      put(96, InputConstants.KEY_GRAVE);
      put(256, InputConstants.KEY_ESCAPE);
      put(257, InputConstants.KEY_RETURN);
      put(258, InputConstants.KEY_TAB);
      put(259, InputConstants.KEY_BACKSPACE);
      put(260, InputConstants.KEY_INSERT);
      put(261, InputConstants.KEY_DELETE);
      put(262, InputConstants.KEY_RIGHT);
      put(263, InputConstants.KEY_LEFT);
      put(264, InputConstants.KEY_DOWN);
      put(265, InputConstants.KEY_UP);
      put(266, InputConstants.KEY_PAGEUP);
      put(267, InputConstants.KEY_PAGEDOWN);
      put(268, InputConstants.KEY_HOME);
      put(269, InputConstants.KEY_END);
      put(280, InputConstants.KEY_CAPSLOCK);
      put(281, InputConstants.KEY_SCROLLLOCK);
      put(282, InputConstants.KEY_NUMLOCK);
      put(283, InputConstants.KEY_PRINTSCREEN);
      put(284, InputConstants.KEY_PAUSE);
      put(340, InputConstants.KEY_LSHIFT);
      put(341, InputConstants.KEY_LCONTROL);
      put(342, InputConstants.KEY_LALT);
      put(343, InputConstants.KEY_LGUI);
      put(344, InputConstants.KEY_RSHIFT);
      put(345, InputConstants.KEY_RCONTROL);
      put(346, InputConstants.KEY_RALT);
      put(347, InputConstants.KEY_RGUI);
   }

   private Keys() {
   }

   private static void put(int glfw, int game) {
      TO_GAME.put(glfw, game);
      TO_GLFW.putIfAbsent(game, glfw);
   }

   /** Game (SDL3) key code for a GLFW code; -1 when the key has no 26.3 equivalent. */
   public static int toGame(int glfw) {
      if (glfw < 0) return -1;
      return TO_GAME.getOrDefault(glfw, -1);
   }

   /** GLFW code for a game key code; -1 when unknown. */
   public static int toGlfw(int game) {
      if (game < 0) return -1;
      return TO_GLFW.getOrDefault(game, -1);
   }

   /** Keyboard key for a GLFW code, or UNKNOWN. */
   public static InputConstants.Key key(int glfw) {
      int game = toGame(glfw);
      return game < 0 ? InputConstants.UNKNOWN : InputConstants.Type.KEYBOARD.getOrCreate(game);
   }
}
