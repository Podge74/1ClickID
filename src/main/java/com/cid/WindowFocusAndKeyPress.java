package main.java.com.cid;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

public class WindowFocusAndKeyPress {

    public interface User32Ex extends StdCallLibrary {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class);

        boolean SetForegroundWindow(HWND hWnd);
        void keybd_event(byte bVk, byte bScan, int dwFlags, Pointer dwExtraInfo);

        int KEYEVENTF_KEYUP = 0x0002;
    }

    public static void pressKey(byte key, boolean isKeyUp) throws InterruptedException {
        int dwFlags = isKeyUp ? 0x02 : 0; // 0x02 is KEYEVENTF_KEYUP
        WindowFocusAndKeyPress.User32Ex.INSTANCE.keybd_event(key, (byte) 0, dwFlags, null);
        Thread.sleep(50);
    }



    public static void pressKeyOld(byte key) throws InterruptedException {
        User32Ex.INSTANCE.keybd_event(key, (byte) 0, 0, null); // Key down
        Thread.sleep(50);
        User32Ex.INSTANCE.keybd_event(key, (byte) 0, User32Ex.KEYEVENTF_KEYUP, null); // Key up
        Thread.sleep(50);
    }

    public static HWND findWindow(String lpClassName, String lpWindowName) {
        return User32.INSTANCE.FindWindow(lpClassName, lpWindowName);
    }

    public static boolean setForegroundWindow(HWND hWnd) {
        return User32.INSTANCE.SetForegroundWindow(hWnd);
    }
}
