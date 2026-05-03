package mods.flammpfeil.slashblade.capability.slashblade;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BladeRuntimeStateStore {
    private static final ConcurrentMap<StackIdentityKey, BladeRuntimeState> store = new ConcurrentHashMap<>();
    private static final ReferenceQueue<ItemStack> refQueue = new ReferenceQueue<>();

    public static BladeRuntimeState get(ItemStack stack) {
        expunge();
        return store.computeIfAbsent(new StackIdentityKey(stack, refQueue), k -> new BladeRuntimeState());
    }

    public static void remove(ItemStack stack) {
        expunge();
        store.remove(new StackIdentityKey(stack, null));
    }

    private static void expunge() {
        Reference<? extends ItemStack> ref;
        while ((ref = refQueue.poll()) != null) {
            store.remove(ref);
        }
    }

    private static class StackIdentityKey extends WeakReference<ItemStack> {
        private final int hash;

        StackIdentityKey(ItemStack stack, @Nullable ReferenceQueue<ItemStack> q) {
            super(stack, q);
            this.hash = System.identityHashCode(stack);
        }

        @Override
        public int hashCode() { return hash; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof StackIdentityKey other)) return false;
            ItemStack thisStack = get();
            ItemStack otherStack = other.get();
            return thisStack != null && thisStack == otherStack;
        }
    }
}
