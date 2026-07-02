package dev.fixyl.dashboard.constant;

public final class Paths {

    public static final String ETC_OSRELEASE = "etc/os-release";

    public static final String PROC_CPUINFO = "proc/cpuinfo";
    public static final String PROC_KERNEL_HOSTNAME = "proc/sys/kernel/hostname";
    public static final String PROC_KERNEL_RELEASE = "proc/sys/kernel/osrelease";
    public static final String PROC_STAT = "proc/stat";

    public static final String RUN_SHUTDOWN_DIR = "run/systemd/shutdown";
    public static final String RUN_SHUTDOWN_SCHEDULED = "run/systemd/shutdown/scheduled";

    public static final String SYS_CPU_CACHE_DIR_TEMPLATE = "sys/devices/system/cpu/cpu%s/cache";
    public static final String SYS_CPU_FREQ_BASE_TEMPLATE = "sys/devices/system/cpu/cpu%s/cpufreq/base_frequency";
    public static final String SYS_CPU_FREQ_CURRENT_TEMPLATE = "sys/devices/system/cpu/cpu%s/cpufreq/scaling_cur_freq";
    public static final String SYS_CPU_FREQ_MAX_TEMPLATE = "sys/devices/system/cpu/cpu%s/cpufreq/scaling_max_freq";
    public static final String SYS_CPU_FREQ_MIN_TEMPLATE = "sys/devices/system/cpu/cpu%s/cpufreq/scaling_min_freq";
    public static final String SYS_CPU_ONLINE = "sys/devices/system/cpu/online";
    public static final String SYS_CPU_TOPO_CORE_ID_TEMPLATE = "sys/devices/system/cpu/cpu%s/topology/core_id";
    public static final String SYS_CPU_TOPO_PACKAGE_ID_TEMPLATE = "sys/devices/system/cpu/cpu%s/topology/physical_package_id";

    private Paths() {}

}
