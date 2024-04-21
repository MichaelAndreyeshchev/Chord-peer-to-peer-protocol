/* FNV-1a Hash --  Fowler/Noll/Vo Hash function */

import java.util.*;

public class FNV1aHash {

    private static final int FNV_32_INIT = 0x811c9dc5;   // 2166136261
    private static final int FNV_32_PRIME = 0x01000193;  // 16777619;

    public static int hash32(String Key) {   // FNV-1a Hash
        byte[] kBytes = Key.getBytes();
        int hash = FNV_32_INIT;
        final int len = kBytes.length;
        for(int i = 0; i < len; i++) {
            hash ^= kBytes[i];
            hash *= FNV_32_PRIME;
        }

        if (hash < 0) {  
           if (hash == Integer.MIN_VALUE) {
               hash = Integer.MAX_VALUE;
           }
           else hash = Math.abs(hash);  
        }
        return hash;
   }

   public static void main(String[] argv ) {
      int hashval = hash32( argv[0]  );
      System.out.println( "Hash value using hash32 is " + hashval );
   }

}

