import java.nio.ByteBuffer;

public class Hash {
    private static final int[] W = new int[64];  //массив 16 слов плюс 48 дополнительных
    private static final int[] TEMP = new int[8]; // массив для временной обработки
    private static final int[] H = new int[8]; // возвращаемый массив хэша

    //Инициализация переменных (первые 32 бита дробных частей  квадратных корней первых восьми простых чисел [от 2 до 19]):
    private static final int[] H0 = {
            0x6A09E667,
            0xBB67AE85,
            0x3C6EF372,
            0xA54FF53A,
            0x510E527F,
            0x9B05688C,
            0x1F83D9AB,
            0x5BE0CD19
    };

    //Таблица констант (первые 32 бита дробных частей кубических корней первых 64-х простых чисел [от 2 до 311])
    private final static int[] K = {
            0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5, 0x3956C25B, 0x59F111F1, 0x923F82A4, 0xAB1C5ED5,
            0xD807AA98, 0x12835B01, 0x243185BE, 0x550C7DC3, 0x72BE5D74, 0x80DEB1FE, 0x9BDC06A7, 0xC19BF174,
            0xE49B69C1, 0xEFBE4786, 0x0FC19DC6, 0x240CA1CC, 0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA,
            0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7, 0xC6E00BF3, 0xD5A79147, 0x06CA6351, 0x14292967,
            0x27B70A85, 0x2E1B2138, 0x4D2C6DFC, 0x53380D13, 0x650A7354, 0x766A0ABB, 0x81C2C92E, 0x92722C85,
            0xA2BFE8A1, 0xA81A664B, 0xC24B8B70, 0xC76C51A3, 0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070,
            0x19A4C116, 0x1E376C08, 0x2748774C, 0x34B0BCB5, 0x391C0CB3, 0x4ED8AA4A, 0x5B9CCA4F, 0x682E6FF3,
            0x748F82EE, 0x78A5636F, 0x84C87814, 0x8CC70208, 0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2
    };

    //расширение текста
    private static byte[] addPad(byte[] firstText) {
        int mesLength = 8; //длина исходного сообщения в битах в виде 64-битного числа с порядком байтов от старшего к младшему
        int newLength = firstText.length + mesLength + 1; //с учетом бита 1
        int nullBytes = 64 - (newLength % 64); //определяем сколько будет дописано нулевых байтов
        newLength = newLength + nullBytes;

        byte[] resText = new byte[newLength];
        System.arraycopy(firstText, 0, resText, 0, firstText.length);
        resText[firstText.length] = (byte) 0b10000000; //записываем байт с 1 битом

        byte[] sizeOfFirstMessage = ByteBuffer.allocate(8).putLong(firstText.length*8).array(); //записываю размер исходного сообщения в битах
        System.arraycopy(sizeOfFirstMessage, 0, resText, resText.length-8, 8);
        return resText;
    }

    // (4 bytes become 1 int)
    private static int[] toIntArray(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < result.length; ++i) {
            result[i] = buf.getInt();
        }
        return result;
    }

    private static int smallSig0(int x) {
        return Integer.rotateRight(x, 7) ^ Integer.rotateRight(x, 18) ^ (x >>> 3);
    }

    private static int smallSig1(int x) {
        return Integer.rotateRight(x, 17) ^ Integer.rotateRight(x, 19) ^ (x >>> 10);
    }

    private static int bigSig0(int x) {
        return Integer.rotateRight(x, 2) ^ Integer.rotateRight(x, 13) ^ Integer.rotateRight(x, 22);
    }

    private static int ma(int x, int y, int z) {
        return (x & y) | (x & z) | (y & z);
    }

    private static int bigSig1(int x) {
        return Integer.rotateRight(x, 6) ^ Integer.rotateRight(x, 11) ^ Integer.rotateRight(x, 25);
    }

    private static int ch(int x, int y, int z) {
        return (x & y) | ((~x) & z);
    }

    // переводит из массива Int[] в массив byte[]
    private static byte[] toByteArray(int[] intArray) {
        ByteBuffer buf = ByteBuffer.allocate(intArray.length * 4);
        for (int i = 0; i < intArray.length; ++i) {
            buf.putInt(intArray[i]);
        }
        return buf.array();
    }

    // основная функция подсчета хэша
    public static byte[] hash(byte[] message) {
        System.arraycopy(H0, 0, H, 0, H0.length);
        byte[] fixMessage = addPad(message);
        int[] fourByteMes = toIntArray(fixMessage);
        //Далее сообщения обрабатывается последовательными порциями по 512 бит: разбить сообщение на куски по 512 бит
        // для каждого куска разбить кусок на 16 слов длиной 32 бита(сделано в toIntArray
        for (int i = 0; i < (fourByteMes.length / 16); i++) {
            System.arraycopy(fourByteMes, i * 16, W, 0, 16);
            //Сгенерировать дополнительные 48 слов:
            for (int t = 16; t < 64; ++t) {
             /*
                s0 := (w[i-15] rotr 7) xor (w[i-15] rotr 18) xor (w[i-15] shr 3)
                s1 := (w[i-2] rotr 17) xor (w[i-2] rotr 19) xor (w[i-2] shr 10)
                w[i] := w[i-16] + s0 + w[i-7] + s1
              */
                W[t] = smallSig1(W[t - 2]) + W[t - 7] + smallSig0(W[t - 15]) + W[t - 16];
            }
            // let TEMP = H
            System.arraycopy(H, 0, TEMP, 0, H.length); //Copy TEMP = ABCDEFGH
            // operate on TEMP
            for (int t = 0; t < W.length; ++t) {
                /*
                    Σ0 := (a rotr 2) xor (a rotr 13) xor (a rotr 22)
                    Ma := (a and b) xor (a and c) xor (b and c)
                    t2 := Σ0 + Ma
                 */
                int t2 = bigSig0(TEMP[0]) + ma(TEMP[0], TEMP[1], TEMP[2]);
                /*
                    Σ1 := (e rotr 6) xor (e rotr 11) xor (e rotr 25)
                    Ch := (e and f) xor ((not e) and g)
                    t1 := h + Σ1 + Ch + k[i] + w[i]
                 */
                int t1 = TEMP[7] + bigSig1(TEMP[4]) + ch(TEMP[4], TEMP[5], TEMP[6]) + K[t] + W[t];
                /*
                    h := g
                    g := f
                    f := e
                    e := d + t1
                    d := c
                    c := b
                    b := a
                    a := t1 + t2
                 */
                System.arraycopy(TEMP, 0, TEMP, 1, TEMP.length - 1);
                TEMP[4] = TEMP[4] + t1;
                TEMP[0] = t1 + t2;
            }
            // Получить итоговое значение хеша: hash = h0 ǁ h1 ǁ h2 ǁ h3 ǁ h4 ǁ h5 ǁ h6 ǁ h7
            for (int t = 0; t < H.length; ++t) {
                H[t] += TEMP[t];
            }
        }
        return toByteArray(H);
    }
}
