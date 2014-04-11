/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package net.onrc.openvirtex.elements.port;

import java.util.HashSet;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFPortFeatures;

/**
 * The Class PortFeatures. This class is useful to translate the port features exposed by the port
 * to sub-features, to simplify get/set operations
 */
public class PortFeatures {
	private Set<OFPortFeatures> portfeatures;
	
    /** The speed 10 Mbps half-duplex. */
    //private boolean speed10MHD      = false;
    
    /** The speed 10 Mbps full-duplex. */
    //private boolean speed10MFD      = false;
    
    /** The speed 100 Mbps half-duplex. */
    //private boolean speed100MHD     = false;
    
    /** The speed 100 Mbps full-duplex. */
    //private boolean speed100MFD     = false;
    
    /** The speed 1 Gbps half-duplex. */
    //private boolean speed1GHD       = false;
    
    /** The speed 1 Gbps full-duplex. */
    //private boolean speed1GFD       = false;
    
    /** The speed 10 Gbps full-duplex. */
    //private boolean speed10GFD      = false;
    
    /** The copper interface. */
    //private boolean copper          = false;
    
    /** The fiber interface. */
    //private boolean fiber           = false;
    
    /** The autonegotiation. */
    //private boolean autonegotiation = false;
    
    /** The pause. */
    //private boolean pause           = false;
    
    /** The pause asym. */
    //private boolean pauseAsym       = false;

    /**
     * Instantiates a new port features.
     */
    public PortFeatures() {
    	portfeatures = new HashSet<OFPortFeatures>();
    	/*
    	this.speed10MHD = false;
    	this.speed10MFD = false;
    	this.speed100MHD = false;
    	this.speed100MFD = false;
    	this.speed1GHD = false;
    	this.speed1GFD = false;
    	this.speed10GFD = false;
    	this.copper = false;
    	this.fiber = false;
    	this.autonegotiation = false;
    	this.pause = false;
    	this.pauseAsym = false;
    	*/
    }

    /**
     * Instantiates a new port features.
     *
     * @param features the features
     */
    
    public PortFeatures(final Set<OFPortFeatures> features) {
    	this.portfeatures = features; 
    }
    /**
     * Instantiates a new port features.
     *
     * @param features the features
     */
    /*
    public PortFeatures(final int features) {
	if ((features & 1 << 0) != 0) {
	    this.speed10MHD = true;
	}
	if ((features & 1 << 1) != 0) {
	    this.speed10MFD = true;
	}
	if ((features & 1 << 2) != 0) {
	    this.speed100MHD = true;
	}
	if ((features & 1 << 3) != 0) {
	    this.speed100MFD = true;
	}
	if ((features & 1 << 4) != 0) {
	    this.speed1GHD = true;
	}
	if ((features & 1 << 5) != 0) {
	    this.speed1GFD = true;
	}
	if ((features & 1 << 6) != 0) {
	    this.speed10GFD = true;
	}
	if ((features & 1 << 7) != 0) {
	    this.copper = true;
	}
	if ((features & 1 << 8) != 0) {
	    this.fiber = true;
	}
	if ((features & 1 << 9) != 0) {
	    this.autonegotiation = true;
	}
	if ((features & 1 << 10) != 0) {
	    this.pause = true;
	}
	if ((features & 1 << 11) != 0) {
	    this.pauseAsym = true;
	}
    }
    */
    /**
     * Sets the current ovx port features.
     */
    public void setCurrentOVXPortFeatures() {
    	portfeatures.clear();
    	portfeatures.add(OFPortFeatures.PF_COPPER);
    	portfeatures.add(OFPortFeatures.PF_1GB_FD);
    	
    	/*
		this.speed10MHD = false;
		this.speed10MFD = false;
		this.speed100MHD = false;
		this.speed100MFD = false;
		this.speed1GHD = false;
		this.speed1GFD = true;
		this.speed10GFD = false;
		this.copper = true;
		this.fiber = false;
		this.autonegotiation = false;
		this.pause = false;
		this.pauseAsym = false;
		*/
    }

    /**
     * Sets the supported ovx port features.
     */
    public void setSupportedOVXPortFeatures() {
    	portfeatures.clear();
    	
    	portfeatures.add(OFPortFeatures.PF_10MB_HD);
    	portfeatures.add(OFPortFeatures.PF_10MB_FD);
    	portfeatures.add(OFPortFeatures.PF_100MB_HD);
    	portfeatures.add(OFPortFeatures.PF_100MB_FD);
    	portfeatures.add(OFPortFeatures.PF_1GB_HD);
    	portfeatures.add(OFPortFeatures.PF_1GB_FD);
    	portfeatures.add(OFPortFeatures.PF_COPPER);

    	/*
    	this.speed10MHD = true;
		this.speed10MFD = true;
		this.speed100MHD = true;
		this.speed100MFD = true;
		this.speed1GHD = true;
		this.speed1GFD = true;
		this.speed10GFD = false;
		this.copper = true;
		this.fiber = false;
		this.autonegotiation = false;
		this.pause = false;
		this.pauseAsym = false;
		*/
    }

    /**
     * Sets the advertised ovx port features.
     */
    public void setAdvertisedOVXPortFeatures() {
    	portfeatures.clear();
    	
    	portfeatures.add(OFPortFeatures.PF_10MB_FD);
    	portfeatures.add(OFPortFeatures.PF_100MB_FD);
    	portfeatures.add(OFPortFeatures.PF_1GB_FD);
    	portfeatures.add(OFPortFeatures.PF_COPPER);
    
    	/*
		this.speed10MHD = false;
		this.speed10MFD = true;
		this.speed100MHD = false;
		this.speed100MFD = true;
		this.speed1GHD = false;
		this.speed1GFD = true;
		this.speed10GFD = false;
		this.copper = true;
		this.fiber = false;
		this.autonegotiation = false;
		this.pause = false;
		this.pauseAsym = false;
		*/
    }

    /**
     * Sets the peer ovx port features.
     */
    public void setPeerOVXPortFeatures() {
    	portfeatures.clear();
    	/*
		this.speed10MHD = false;
		this.speed10MFD = false;
		this.speed100MHD = false;
		this.speed100MFD = false;
		this.speed1GHD = false;
		this.speed1GFD = false;
		this.speed10GFD = false;
		this.copper = false;
		this.fiber = false;
		this.autonegotiation = false;
		this.pause = false;
		this.pauseAsym = false;
		*/
    }

    /**
     * Gets the oVX features.
     *
     * @return the oVX features
     */
    public Set<OFPortFeatures> getOVXFeatures() {
    	return portfeatures;
    }
    /**
     * Gets the oVX features.
     *
     * @return the oVX features
     */
    /*
    public Integer getOVXFeatures() {
	Integer features = 0;
	if (this.speed10MHD) {
	    features += OFPortFeatures.PF_10MB_HD .getValue();
	}
	if (this.speed10MFD) {
	    features += OFPortFeatures.PF_10MB_FD.getValue();
	}
	if (this.speed100MHD) {
	    features += OFPortFeatures.PF_100MB_HD.getValue();
	}
	if (this.speed100MFD) {
	    features += OFPortFeatures.PF_100MB_FD.getValue();
	}
	if (this.speed1GHD) {
	    features += OFPortFeatures.PF_1GB_HD.getValue();
	}
	if (this.speed1GFD) {
	    features += OFPortFeatures.PF_1GB_FD.getValue();
	}
	if (this.speed10GFD) {
	    features += OFPortFeatures.PF_10GB_FD.getValue();
	}
	if (this.copper) {
	    features += OFPortFeatures.PF_COPPER.getValue();
	}
	if (this.fiber) {
	    features += OFPortFeatures.PF_FIBER.getValue();
	}
	if (this.autonegotiation) {
	    features += OFPortFeatures.PF_AUTONEG.getValue();
	}
	if (this.pause) {
	    features += OFPortFeatures.PF_PAUSE.getValue();
	}
	if (this.pauseAsym) {
	    features += OFPortFeatures.PF_PAUSE_ASYM.getValue();
	}
	return features;
    }
	*/
    
    /**
     * Checks if is speed10 mhd.
     *
     * @return true, if is speed10 mhd
     */
    public boolean isSpeed10MHD() {
    	return portfeatures.contains(OFPortFeatures.PF_10MB_HD);
    	//return this.speed10MHD;
    }

    /**
     * Sets the speed10 mhd.
     *
     * @param speed10mhd the new speed10 mhd
     */
    public void setSpeed10MHD(final boolean speed10mhd) {
    	if(speed10mhd)
    	{
    		portfeatures.add(OFPortFeatures.PF_10MB_HD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_10MB_HD);
    	}
    	//this.speed10MHD = speed10mhd;
    }

    /**
     * Checks if is speed10 mfd.
     *
     * @return true, if is speed10 mfd
     */
    public boolean isSpeed10MFD() {
    	return portfeatures.contains(OFPortFeatures.PF_10MB_FD);
    	//return this.speed10MFD;
    }

    /**
     * Sets the speed10 mfd.
     *
     * @param speed10mfd the new speed10 mfd
     */
    public void setSpeed10MFD(final boolean speed10mfd) {
    	if(speed10mfd)
    	{
    		portfeatures.add(OFPortFeatures.PF_10MB_FD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_10MB_FD);
    	}
    	//this.speed10MFD = speed10mfd;
    }

    /**
     * Checks if is speed100 mhd.
     *
     * @return true, if is speed100 mhd
     */
    public boolean isSpeed100MHD() {
    	return portfeatures.contains(OFPortFeatures.PF_100MB_HD);
    	//return this.speed100MHD;
    }

    /**
     * Sets the speed100 mhd.
     *
     * @param speed100mhd the new speed100 mhd
     */
    public void setSpeed100MHD(final boolean speed100mhd) {
    	if (speed100mhd)
    	{
    		portfeatures.add(OFPortFeatures.PF_100MB_HD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_100MB_HD);
    	}
    	//this.speed100MHD = speed100mhd;
    }

    /**
     * Checks if is speed100 mfd.
     *
     * @return true, if is speed100 mfd
     */
    public boolean isSpeed100MFD() {
    	return portfeatures.contains(OFPortFeatures.PF_100MB_FD);
    	//return this.speed100MFD;
    }

    /**
     * Sets the speed100 mfd.
     *
     * @param speed100mfd the new speed100 mfd
     */
    public void setSpeed100MFD(final boolean speed100mfd) {
    	if (speed100mfd)
    	{
    		portfeatures.add(OFPortFeatures.PF_100MB_FD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_100MB_FD);
    	}
    	//this.speed100MFD = speed100mfd;
    }

    /**
     * Checks if is speed1 ghd.
     *
     * @return true, if is speed1 ghd
     */
    public boolean isSpeed1GHD() {
    	return portfeatures.contains(OFPortFeatures.PF_1GB_HD);
    	//return this.speed1GHD;
    }

    /**
     * Sets the speed1 ghd.
     *
     * @param speed1ghd the new speed1 ghd
     */
    public void setSpeed1GHD(final boolean speed1ghd) {
    	if(speed1ghd)
    	{
    		portfeatures.add(OFPortFeatures.PF_1GB_HD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_1GB_HD);
    	}
    	//this.speed1GHD = speed1ghd;
    }

    /**
     * Checks if is speed1 gfd.
     *
     * @return true, if is speed1 gfd
     */
    public boolean isSpeed1GFD() {
    	return portfeatures.contains(OFPortFeatures.PF_1GB_FD);
    	//return this.speed1GFD;
    }

    /**
     * Sets the speed1 gfd.
     *
     * @param speed1gfd the new speed1 gfd
     */
    public void setSpeed1GFD(final boolean speed1gfd) {
    	if(speed1gfd)
    	{
    		portfeatures.add(OFPortFeatures.PF_1GB_FD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_1GB_FD);
    	}
    	//this.speed1GFD = speed1gfd;
    }

    /**
     * Checks if is speed10 gfd.
     *
     * @return true, if is speed10 gfd
     */
    public boolean isSpeed10GFD() {
    	return portfeatures.contains(OFPortFeatures.PF_10GB_FD);
    	//return this.speed10GFD;
    }

    /**
     * Sets the speed10 gfd.
     *
     * @param speed10gfd the new speed10 gfd
     */
    public void setSpeed10GFD(final boolean speed10gfd) {
    	if(speed10gfd)
    	{
    		portfeatures.add(OFPortFeatures.PF_10GB_FD);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_10GB_FD);
    	}
    	//this.speed10GFD = speed10gfd;
    }

    /**
     * Checks if is copper.
     *
     * @return true, if is copper
     */
    public boolean isCopper() {
    	return portfeatures.contains(OFPortFeatures.PF_COPPER);
    	//return this.copper;
    }

    /**
     * Sets the copper.
     *
     * @param copper the new copper
     */
    public void setCopper(final boolean copper) {
    	if(copper)
    	{
    		portfeatures.add(OFPortFeatures.PF_COPPER);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_COPPER);
    	}
    	//this.copper = copper;
    }

    /**
     * Checks if is fiber.
     *
     * @return true, if is fiber
     */
    public boolean isFiber() {
    	return portfeatures.contains(OFPortFeatures.PF_FIBER);
    	//return this.fiber;
    }

    /**
     * Sets the fiber.
     *
     * @param fiber the new fiber
     */
    public void setFiber(final boolean fiber) {
    	if(fiber)
    	{
    		portfeatures.add(OFPortFeatures.PF_FIBER);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_FIBER);
    	}
    	//this.fiber = fiber;
    }

    /**
     * Checks if is autonegotiation.
     *
     * @return true, if is autonegotiation
     */
    public boolean isAutonegotiation() {
    	return portfeatures.contains(OFPortFeatures.PF_AUTONEG);
    	//return this.autonegotiation;
    }

    /**
     * Sets the autonegotiation.
     *
     * @param autonegotiation the new autonegotiation
     */
    public void setAutonegotiation(final boolean autonegotiation) {
    	if(autonegotiation)
    	{
    		portfeatures.add(OFPortFeatures.PF_AUTONEG);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_AUTONEG);
    	}
    	//this.autonegotiation = autonegotiation;
    }

    /**
     * Checks if is pause.
     *
     * @return true, if is pause
     */
    public boolean isPause() {
    	return portfeatures.contains(OFPortFeatures.PF_PAUSE);
    	//return this.pause;
    }

    /**
     * Sets the pause.
     *
     * @param pause the new pause
     */
    public void setPause(final boolean pause) {
    	if(pause)
    	{
    		portfeatures.add(OFPortFeatures.PF_PAUSE);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_PAUSE);
    	}
    	//this.pause = pause;
    }

    /**
     * Checks if is pause asym.
     *
     * @return true, if is pause asym
     */
    public boolean isPauseAsym() {
    	return portfeatures.contains(OFPortFeatures.PF_PAUSE_ASYM);
    	//return this.pauseAsym;
    }

    /**
     * Sets the pause asym.
     *
     * @param pauseAsym the new pause asym
     */
    public void setPauseAsym(final boolean pauseAsym) {
    	if(pauseAsym)
    	{
    		portfeatures.add(OFPortFeatures.PF_PAUSE_ASYM);
    	} else {
    		portfeatures.remove(OFPortFeatures.PF_PAUSE_ASYM);
    	}
    	//this.pauseAsym = pauseAsym;
    }

    /**
     * Gets the highest throughput exposed by the port
     *
     * @return the highest throughput
     */
    public Integer getHighestThroughput() {
	if (portfeatures.contains(OFPortFeatures.PF_10GB_FD))
		return 10000;
	
	if (portfeatures.contains(OFPortFeatures.PF_1GB_FD))
		return 1000;
	
	if (portfeatures.contains(OFPortFeatures.PF_1GB_HD))
		return 500;
	
	if (portfeatures.contains(OFPortFeatures.PF_100MB_FD))
		return 100;
	
	if (portfeatures.contains(OFPortFeatures.PF_100MB_HD))
		return 50;
	
	if (portfeatures.contains(OFPortFeatures.PF_10MB_FD))
		return 10;
	
	if (portfeatures.contains(OFPortFeatures.PF_10MB_HD))
		return 5;
	
	return 1;
	/*
	 Integer thr = 1;
	if (this.speed10MHD) {
	    thr = 5;
	}
	if (this.speed10MFD) {
	    thr = 10;
	}
	if (this.speed100MHD) {
	    thr = 50;
	}
	if (this.speed100MFD) {
	    thr = 100;
	}
	if (this.speed1GHD) {
	    thr = 500;
	}
	if (this.speed1GFD) {
	    thr = 1000;
	}
	if (this.speed10GFD) {
	    thr = 10000;
	}
	return thr;*/
    }
}
