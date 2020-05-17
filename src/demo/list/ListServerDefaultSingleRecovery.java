/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and
 * the authors indicated in the @author tags
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package demo.list;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.demo.listvalue.BFTListImpl;
import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import parallelism.MessageContextPair;
import parallelism.ParallelMapping;
import parallelism.ParallelServiceReplica;
import parallelism.SequentialServiceReplica;
import parallelism.late.CBASEServiceReplica;
import parallelism.late.COSType;
import parallelism.late.ConflictDefinition;

public final class ListServerDefaultSingleRecovery extends DefaultSingleRecoverable {

	private LinkedList<Integer> existentList = new LinkedList<Integer>();
	private int counter = 0;
	private int iterations = 0;

	public static void main(String[] args) {
		if (args.length < 7) {
			System.out.println(
					"Usage: ... ListServer <processId> <num threads> <initial entries> <late scheduling?> <graph type>");
			System.exit(-1);
		}
		int processId = Integer.parseInt(args[0]);
		int initialNT = Integer.parseInt(args[1]);
		int entries = Integer.parseInt(args[2]);
		boolean late = Boolean.parseBoolean(args[3]);
		String gType = args[4];

		new ListServerDefaultSingleRecovery(processId, initialNT, entries, late, gType);
	}

	public ListServerDefaultSingleRecovery(int id, int initThreads, int entries, boolean late, String gType) {

		if (initThreads <= 0) {
			System.out.println("Replica in sequential execution model.");

			// new ServiceReplica(id, this, null);
			new SequentialServiceReplica(id, this, this);

		} else if (late) {
			System.out.println("Replica in parallel execution model (late scheduling).");
			ConflictDefinition cd = new ConflictDefinition() {
				@Override
				public boolean isDependent(MessageContextPair r1, MessageContextPair r2) {
					if (r1.classId == ParallelMapping.SYNC_ALL || r2.classId == ParallelMapping.SYNC_ALL) {
						return true;
					}
					return false;
				}
			};

			if (gType.equals("coarseLock")) {
				new CBASEServiceReplica(id, this, this, initThreads, cd, COSType.coarseLockGraph);
			} else if (gType.equals("fineLock")) {
				new CBASEServiceReplica(id, this, this, initThreads, cd, COSType.fineLockGraph);
			} else if (gType.equals("lockFree")) {
				new CBASEServiceReplica(id, this, this, initThreads, cd, COSType.lockFreeGraph);
			} else {
				new CBASEServiceReplica(id, this, this, initThreads, cd, null);
			}
		} else {
			System.out.println("Replica in parallel execution model (early scheduling).");

			new ParallelServiceReplica(id, this, null, initThreads);
			// replica = new ParallelServiceReplica(id, this, null, minThreads, initThreads,
			// maxThreads, new LazyPolicy());
			// replica = new ParallelServiceReplica(id, this,this, minThreads, initThreads,
			// maxThreads, new AgressivePolicy());

		}
		for (int i = 0; i < entries; i++) {
			existentList.add(i);
		}

		System.out.println("Server initialization complete!");
	}

	public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
		return execute(command, msgCtx);
	}

	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		return execute(command, msgCtx);
	}

	public byte[] execute(byte[] command, MessageContext msgCtx) {

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(command);
			ByteArrayOutputStream out = null;
			byte[] reply = null;
			int cmd = new DataInputStream(in).readInt();

			switch (cmd) {
			case BFTList.ADD:
				Integer value = (Integer) new ObjectInputStream(in).readObject();
				boolean ret = false;
				if (!existentList.contains(value)) {
					ret = existentList.add(value);
				}

				// Thread.sleep(2000);

				out = new ByteArrayOutputStream();
				ObjectOutputStream out1 = new ObjectOutputStream(out);
				out1.writeBoolean(ret);
				out.flush();
				out1.flush();
				reply = out.toByteArray();
				break;
			case BFTList.REMOVE:
				value = (Integer) new ObjectInputStream(in).readObject();
				ret = existentList.remove(value);
				out = new ByteArrayOutputStream();
				out1 = new ObjectOutputStream(out);
				out1.writeBoolean(ret);
				out.flush();
				out1.flush();
				reply = out.toByteArray();
				break;
			case BFTList.SIZE:
				out = new ByteArrayOutputStream();
				new DataOutputStream(out).writeInt(existentList.size());
				reply = out.toByteArray();
				break;
			case BFTList.CONTAINS:
				value = (Integer) new ObjectInputStream(in).readObject();
				out = new ByteArrayOutputStream();
				out1 = new ObjectOutputStream(out);
				out1.writeBoolean(existentList.contains(value));

				/*
				 * out1.writeBoolean(true);
				 * 
				 * Iterator<Integer> it = l.iterator();
				 * 
				 * while(it.hasNext()){ it.next(); }
				 */

				out.flush();
				out1.flush();
				reply = out.toByteArray();
				break;
			case BFTList.GET:
				int index = new DataInputStream(in).readInt();
				Integer r = null;
				if (index > existentList.size()) {
					r = new Integer(-1);
				} else {
					r = existentList.get(index);
				}
				out = new ByteArrayOutputStream();
				out1 = new ObjectOutputStream(out);
				out1.writeObject(r);
				reply = out.toByteArray();
				break;
			}
			return reply;
		} catch (Exception ex) {
			java.util.logging.Logger.getLogger(ListServerDefaultSingleRecovery.class.getName()).log(Level.SEVERE, null,
					ex);
			return null;
		}

	}

	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		iterations++;
		System.out.println("(" + iterations + ") Counter current value: " + counter);
		return execute(command, msgCtx);
	}

	@Override
	public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
		iterations++;
		return execute(command, msgCtx);
	}

	@Override
	public void installSnapshot(byte[] state) {
		try {

			// serialize to byte array and return
			ByteArrayInputStream bis = new ByteArrayInputStream(state);
			ObjectInput in = new ObjectInputStream(bis);
			existentList = (LinkedList<Integer>) in.readObject();
			in.close();
			bis.close();

		} catch (ClassNotFoundException ex) {
			Logger.getLogger(BFTListImpl.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BFTListImpl.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public byte[] getSnapshot() {
		try {

			// System.out.println("[getSnapshot] tables: " + tableMap.getSizeofTable());
			// serialize to byte array and return
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(existentList);

			out.flush();
			bos.flush();
			out.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(BFTListImpl.class.getName()).log(Level.SEVERE, null, ex);
			return new byte[0];
		}
	}
}
