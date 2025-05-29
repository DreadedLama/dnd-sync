package in.dreadedlama.dndsync.shared;

import java.io.Serializable;

public class WearSignal implements Serializable {

    public Integer dndState = null;

    public WearSignal(Integer dndState) {

        // DnD disabled:
        // 0 = INTERRUPTION_FILTER_UNKNOWN
        // 1 = INTERRUPTION_FILTER_ALL

        // DnD enabled:
        // 2 = INTERRUPTION_FILTER_PRIORITY
        // 3 = INTERRUPTION_FILTER_NONE (no notification passes)
        // 4 = INTERRUPTION_FILTER_ALARMS

        // Custom
        // 5 = BEDTIME ON
        // 6 = BEDTIME OFF
        if (0 <= dndState && dndState <= 6) {

            this.dndState = dndState;
        }
    }
}
