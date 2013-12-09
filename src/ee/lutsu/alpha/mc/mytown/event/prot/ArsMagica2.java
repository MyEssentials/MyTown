package ee.lutsu.alpha.mc.mytown.event.prot;

import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class ArsMagica2 extends ProtBase {
    @Override
    public void load() throws Exception {
    }

    @Override
    public boolean loaded() {
    	return false;
    }
    
	@Override
	public String getMod() {
		return "Ars Magics 2";
	}

	@Override
	public String getComment() {
		return "Protects against Ars Magica 2 Spells";
	}
}