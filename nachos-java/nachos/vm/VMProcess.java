package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
    /**
     * Allocate a new process.
     */
    public VMProcess() {
        super();
        //VMKernel.InvertedPageTable.put(processID, pageTable);
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
        super.saveState();

    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        // super.restoreState();
        //invalid all tlb
        Processor processor = Machine.processor();
        for (int i = 0; i < processor.getTLBSize(); i++) {
            processor.writeTLBEntry(i, new TranslationEntry());
        }
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     *
     * @return <tt>true</tt> if successful.
     */
    protected boolean loadSections() {
        return super.loadSections();
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        super.unloadSections();
    }


    private void handleTlbMiss() {
        Lib.debug(dbgVM, "handleTlbMiss!");
        Processor processor = Machine.processor();
        int vpn = processor.readRegister(Processor.regBadVAddr);
        TranslationEntry page = translate(vpn / pageSize);
        //ranodomly update tlb
        int tlbIdx = Lib.random(processor.getTLBSize());
        //update dirty and used bit
        TranslationEntry oldPage = processor.readTLBEntry(tlbIdx);
        //VMKernel.pagePool.getEntryByPaddr(oldPage.ppn).used = oldPage.used;
        //VMKernel.pagePool.getEntryByPaddr(oldPage.ppn).dirty = oldPage.dirty;
        //tlb replacement
        processor.writeTLBEntry(tlbIdx, page);
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param cause the user exception that occurred.
     */
    public void handleException(int cause) {
        Processor processor = Machine.processor();

        switch (cause) {
            case Processor.exceptionTLBMiss:
                handleTlbMiss();
                break;
            default:
                super.handleException(cause);
                break;
        }
    }


//    protected void freeResources() {
//        super.freeResources();
//        VMKernel.InvertedPageTable.remove(processID);
//    }

    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgVM = 'v';
}
