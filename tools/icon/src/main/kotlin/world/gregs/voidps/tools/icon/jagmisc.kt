/* jagmisc - minimal stub.
 *
 * The real jagex3.jagmisc.jagmisc is a native-backed ICMP ping helper used
 * by Class169.run() (a background network-latency-measuring thread). That
 * thread is unrelated to icon rendering and, in this standalone renderer,
 * never actually has work queued onto it - but Class169.run() is genuine
 * (JaCoCo saw the thread start), so the call site must still resolve at
 * compile time. Declared here (in the jagex3.jagmisc package) rather than
 * editing Class169.java's import, since the flat build only compiles
 * *.java directly under src/ - javac does not require this file to live
 * under a jagex3/jagmisc/ subdirectory when passed explicitly.
 */

package world.gregs.voidps.tools.icon;

public final class jagmisc {
    public static int ping(byte b0, byte b1, byte b2, byte b3, long timeout) {
        return 1000;
    }
}
