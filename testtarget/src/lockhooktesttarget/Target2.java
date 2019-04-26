package lockhooktesttarget;

public class Target2 {

    @HookSpec(isNotLockable = true)
    private class LocalService {
    }

    @HookSpec(preEnter = "preEnterHook")
    private class HookTarget {
    }
}
