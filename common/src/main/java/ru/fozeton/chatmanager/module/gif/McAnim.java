package ru.fozeton.chatmanager.module.gif;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface McAnim extends Library {
    McAnim INSTANCE = Native.load("mcanim_impl", McAnim.class);

    boolean convert_webp(
            byte[] data_ptr,
            long len,
            String file_name_ptr,
            String out_dir_ptr
    );
}
