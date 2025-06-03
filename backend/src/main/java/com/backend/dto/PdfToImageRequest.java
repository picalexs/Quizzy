package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdfToImageRequest {
    private String pdfPath;
    private String textPath;
}
