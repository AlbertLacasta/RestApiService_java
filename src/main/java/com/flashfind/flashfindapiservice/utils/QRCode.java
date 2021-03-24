package com.flashfind.flashfindapiservice.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Hashtable;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

public class QRCode {

    /**
     * Generate QR Code and returns in Base64
     *
     * @param qrCodeText
     * @return
     * @throws WriterException
     * @throws IOException
     */
    public static byte[] generateQR(String qrCodeText)
            throws WriterException, IOException
    {
        int size = 225;

        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static String readRQ(byte[] bytes) throws IOException {
        BufferedImage image = ImageIO.read(byteArr2ImputStream(bytes));
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;

        try {
            result = reader.decode(bitmap);
        } catch (ReaderException e) {
            e.printStackTrace();
        }

        return result.getText();
    }

    /***************************************************************/
    /** QR Converter                                              **/
    /***************************************************************/

    /**
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static BufferedImage byteArr2Image(byte[] bytes) throws IOException {
       return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /**
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static String byteArr2Base64(byte[] bytes) {
        return Base64Utils.encodeToString(bytes);
    }

    /**
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static InputStream byteArr2ImputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     *
     * @param bytes
     * @return
     * @throws SQLException
     */
    public static SerialBlob byteArr2Blob(byte[] bytes) throws SQLException {
       return new SerialBlob(bytes);
    }

}
