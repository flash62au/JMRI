package jmri.managers;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.beans.PropertyChangeSupport;
import jmri.implementation.PermissionProgrammer;
import jmri.implementation.PermissionAddressedProgrammer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a very basic implementation of a programmer manager by providing a
 * union of the AddressedProgrammerManager and GlobalProgrammerManager
 * interfaces.
 * <p>
 * This implementation requires a service-mode Programmer at construction time
 * and returns that Programmer for all global programming mode requests. This
 * implementation of AddressedProgrammerManager always returns null for Op Mode,
 * or addressed programmer requests, indicating there is no programmer of that
 * type.
 *
 * @see jmri.AddressedProgrammerManager
 * @see jmri.GlobalProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2001, 2015, 2016
 */
public class DefaultProgrammerManager extends PropertyChangeSupport implements AddressedProgrammerManager, GlobalProgrammerManager {

    // For the record, these were the original numerical definitions:
    //     public static final ProgrammingMode NONE              = new ProgrammingMode("NONE", 0);
    //     public static final ProgrammingMode REGISTERMODE      = new ProgrammingMode("REGISTERMODE", 11);
    //     public static final ProgrammingMode PAGEMODE          = new ProgrammingMode("PAGEMODE", 21);
    //     public static final ProgrammingMode DIRECTBITMODE     = new ProgrammingMode("DIRECTBITMODE", 31);
    //     public static final ProgrammingMode DIRECTBYTEMODE    = new ProgrammingMode("DIRECTBYTEMODE", 32);
    //     public static final ProgrammingMode ADDRESSMODE       = new ProgrammingMode("ADDRESSMODE", 41);
    //     public static final ProgrammingMode OPSBYTEMODE       = new ProgrammingMode("OPSBYTEMODE", 101);
    //     public static final ProgrammingMode OPSBITMODE        = new ProgrammingMode("OPSBITMODE", 102);
    //     public static final ProgrammingMode OPSACCBYTEMODE    = new ProgrammingMode("OPSACCBYTEMODE", 111);
    //     public static final ProgrammingMode OPSACCBITMODE     = new ProgrammingMode("OPSACCBITMODE", 112);
    //     public static final ProgrammingMode OPSACCEXTBYTEMODE = new ProgrammingMode("OPSACCEXTBYTEMODE", 121);
    //     public static final ProgrammingMode OPSACCEXTBITMODE  = new ProgrammingMode("OPSACCEXTBITMODE", 122);
    private Programmer programmer;
    Map<Programmer, PermissionProgrammer> permissionProgrammers = new HashMap<>();
    Map<AddressedProgrammer, PermissionAddressedProgrammer> permissionAddressedProgrammers = new HashMap<>();

    /**
     * Constructor when no global programmer is available.
     */
    public DefaultProgrammerManager() {
        this(null);  // indicates not present
    }

    /**
     * Constructor with a programmer.
     *
     * @param programmer the programmer to use; if null, acts as if no
     *                   programmer is available
     */
    public DefaultProgrammerManager(@CheckForNull Programmer programmer) {
        this.programmer = programmer;
    }

    /**
     * Constructor with a programmer and associated connection.
     *
     * @param programmer the programmer to use; if null, acts as if no
     *                   programmer is available
     * @param memo       the associated connection
     */
    public DefaultProgrammerManager(@CheckForNull Programmer programmer, @Nonnull jmri.SystemConnectionMemo memo) {
        this(programmer);
        this.userName = memo.getUserName();
    }

    private String userName = "<Default>";

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should
     * return a user-provided name for this particular one.
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should
     * return a user-provided name for this particular one.
     */
    @Override
    public String toString() {
        return getUserName();
    }

    @Override
    public final Programmer getGlobalProgrammer() {
        Programmer p = getConcreteGlobalProgrammer();
        if (p != null) {
            return permissionProgrammers.computeIfAbsent(p,
                    v -> new PermissionProgrammer(v));
        } else {
            return null;
        }
    }

    /**
     * Gain access to the Global Mode Programmer without reservation.
     *
     * @return null only if there isn't a Global Mode Programmer available via
     *         this Manager.
     */
    @CheckForNull
    protected Programmer getConcreteGlobalProgrammer() {
        log.debug("return default service-mode programmer of type {}", (programmer != null ? programmer.getClass() : "(null)"));
        return programmer;
    }

    @Override
    public final AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammer p = getConcreteAddressedProgrammer(pLongAddress, pAddress);
        if (p != null) {
            return permissionAddressedProgrammers.computeIfAbsent(p,
                    v -> new PermissionAddressedProgrammer(v));
        } else {
            return null;
        }
    }

    /**
     * Gain access to a Addressed Mode Programmer without reservation.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     specific decoder address to use
     * @return null only if there isn't an Ops Mode Programmer in the system
     */
    @CheckForNull
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    @Override
    public final Programmer reserveGlobalProgrammer() {
        Programmer p = reserveConcreteGlobalProgrammer();
        if (p != null) {
            return permissionProgrammers.computeIfAbsent(p,
                    v -> new PermissionProgrammer(v));
        } else {
            return null;
        }
    }

    /**
     * Gain access to the Global Mode Programmer, in the process reserving it
     * for yourself.
     *
     * @return null if the existing Global Mode programmer is in use
     */
    protected Programmer reserveConcreteGlobalProgrammer() {
        return programmer;
    }

    @Override
    public void releaseGlobalProgrammer(@Nonnull Programmer p) {
    }

    @Override
    public final AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammer p = reserveConcreteAddressedProgrammer(pLongAddress, pAddress);
        if (p != null) {
            return permissionAddressedProgrammers.computeIfAbsent(p,
                    v -> new PermissionAddressedProgrammer(v));
        } else {
            return null;
        }
    }

    /**
     * Gain access to a (the) Addressed Mode Programmer, in the process
     * reserving it for yourself.
     *
     * @param pLongAddress true if this is a long (14 bit) address, else false
     * @param pAddress     Specific decoder address to use
     * @return null if the address is in use by a reserved programmer
     */
    @CheckForNull
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    @Override
    public void releaseAddressedProgrammer(@Nonnull AddressedProgrammer p) {
    }

    /**
     * {@inheritDoc}
     *
     * @return always false in this implementation
     */
    @Override
    public boolean isAddressedModePossible() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return always false in this implementation
     */
    @Override
    public boolean isAddressedModePossible(@Nonnull jmri.LocoAddress l) {
        return isAddressedModePossible();
    }

    /**
     * {@inheritDoc}
     *
     * @return always false in this implementation
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return a default list of programming modes that most
     *         {@link jmri.AddressedProgrammer}s make available
     */
    @Override
    public List<ProgrammingMode> getDefaultModes() {
        List<ProgrammingMode> retval = new java.util.ArrayList<>();
        retval.add(ProgrammingMode.OPSBYTEMODE);
        return retval;
    }

    public void dispose() {
        if (programmer != null) {
            programmer.dispose();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultProgrammerManager.class);
}
