package ru.fozeton.chatmanager.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface McAnim extends Library {
    McAnim INSTANCE = Native.load("mcanim_impl", McAnim.class);

    boolean convert_and_save_webp(
            byte[] data_ptr,
            long len,
            String file_name_ptr,
            String out_dir_ptr
    );
}
