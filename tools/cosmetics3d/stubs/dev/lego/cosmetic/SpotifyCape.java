package dev.lego.cosmetic;
final class SpotifyCape { static final ThreadLocal<Live> LIVE = new ThreadLocal<>(); static final class Live { final double[] bars = new double[11]; final double[] peaks = new double[11]; String title = ""; String artist = ""; double progress; double beat; boolean playing; } }
