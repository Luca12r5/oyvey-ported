package dev.visor.module;

import de.bauloo.styles.BaulooStylesClient.Tab;
import dev.lego.core.Modules;
import dev.lego.txt.CustomTxt;
import dev.lego.txt.TxtModules;
import dev.visor.setting.ModeSetting;

/**
 * A Custom TxT style group in the headset menu. It keeps no state of its own: every change goes to
 * the LEGO module of the same id (which picks the resource pack), and that one mirrors back here.
 */
public final class TxtModule extends Module {
	private final ModeSetting style;
	private boolean mirroring;

	TxtModule(Tab tab) {
		super("txt_" + tab.prefix, tab.title, "Bauloo styles for " + tab.title.toLowerCase(java.util.Locale.ROOT), "palette", Category.CUSTOM_TXT);
		String[][] styles = CustomTxt.styles(tab);
		String[] names = new String[styles.length];
		for (int i = 0; i < styles.length; i++) {
			names[i] = styles[i][1];
		}
		this.style = new ModeSetting("style", "Style", "Which pack this group uses", names[0], names);
		this.style.onChange(() -> {
			TxtModules.StyleModule lego = this.lego();
			if (!this.mirroring && lego != null) {
				lego.style.index = this.style.index();
			}
		});
		this.group("Style", this.style);
	}

	private TxtModules.StyleModule lego() {
		return Modules.ALL.stream()
			.filter(m -> m instanceof TxtModules.StyleModule && m.id.equals(this.id()))
			.map(m -> (TxtModules.StyleModule) m)
			.findFirst()
			.orElse(null);
	}

	@Override
	protected void onEnable() {
		TxtModules.StyleModule lego = this.lego();
		if (!this.mirroring && lego != null) {
			lego.style.index = this.style.index();
			lego.enabled = true;
		}
	}

	@Override
	protected void onDisable() {
		TxtModules.StyleModule lego = this.lego();
		if (!this.mirroring && lego != null) {
			lego.enabled = false;
		}
	}

	/** Shows the LEGO module's state without sending it back. */
	public void mirror(boolean enabled, int index) {
		this.mirroring = true;
		try {
			this.style.set(this.style.modes().get(Math.max(0, Math.min(this.style.modes().size() - 1, index))));
			this.setEnabled(enabled);
		} finally {
			this.mirroring = false;
		}
	}

	/** The groups that ship styles, like the LEGO category. */
	static void registerAll() {
		for (Tab tab : CustomTxt.TABS) {
			if (CustomTxt.styles(tab).length > 0) {
				ModuleManager.addTxt(new TxtModule(tab));
			}
		}
	}
}
