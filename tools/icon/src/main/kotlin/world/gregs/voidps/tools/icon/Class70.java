package world.gregs.voidps.tools.icon;/* Class70 - minimal stub (missing from trimmed tree)
 * See client/src/Class70.java for the full original.
 */

final class Class70 {
    static int[] anIntArray1204;
    static int[] anIntArray1207 = new int[16384];

    static {
        anIntArray1204 = new int[16384];
        double d = 3.834951969714103E-4;
        for (int i = 0; i < 16384; i++) {
            anIntArray1207[i] = (int) (16384.0 * Math.sin(d * (double) i));
            anIntArray1204[i] = (int) (Math.cos(d * (double) i) * 16384.0);
        }
    }
}
