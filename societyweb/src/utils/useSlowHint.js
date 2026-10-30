import { useEffect, useState } from 'react';

export function useSlowHint(active, delayMs = 5000) {
    const [slow, setSlow] = useState(false);

    useEffect(() => {
        if (!active) {
            setSlow(false);
            return undefined;
        }
        const timer = setTimeout(() => setSlow(true), delayMs);
        return () => clearTimeout(timer);
    }, [active, delayMs]);

    return slow;
}