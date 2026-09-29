package ru.fozeton.chatmanager.module.gif;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileContainer {
    private MediaFormat hd;
    private MediaFormat md;
    private MediaFormat sm;
    private MediaFormat xs;
}