/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.hibernate.app;

import com.hazelcast.hibernate.entity.DummyEntity;
import com.hazelcast.hibernate.entity.DummyProperty;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    private final Random random = new Random();

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private SessionFactory sessionFactory;

    public void execute() throws Exception {
        CountDownLatch latch = new CountDownLatch(1000);
        int count;

        Session session = sessionFactory.openSession();
        try {
            Criteria criteria = session.createCriteria(DummyEntity.class);
            criteria.setProjection(Projections.rowCount());
            count = ((Long) criteria.uniqueResult()).intValue();
        } finally {
            session.close();
        }

        if (count == 0) {
            count = 200000;
            insertDummyEntities(count, 100);
        }

        try {
            for (int i = 0; i < latch.getCount(); i++) {
                executorService.submit(new Task(i, sessionFactory, 1000, latch));
            }

            latch.await(1, TimeUnit.DAYS);
        } finally {
            executorService.shutdown();
        }
    }

    protected void insertDummyEntities(int count, int maxChildCount) {
        LOGGER.info("Creating " + count + " demo data in chunks per 1000 elements");

        int chunks = count / 1000 + 1;
        for (int o = 0; o < chunks; o++) {
            int begin = Math.max(0, o * 1000 - 1);
            int end = Math.min((o + 1) * 1000 - 1, count);
            LOGGER.info("Starting chunk #" + o + " (" + begin + "->" + end + ")...");

            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            try {
                for (int i = begin; i < end; i++) {
                    DummyEntity e = new DummyEntity((long) i, "dummy:" + i, i * 123456d, new Date());
                    session.save(e);
                    int childCount = random.nextInt(maxChildCount)+1;
                    for (int j = 0; j < childCount; j++) {
                        DummyProperty p = new DummyProperty("key:" + j, e);
                        session.save(p);
                    }
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                session.close();
            }

            LOGGER.info("Finished chunk #" + o + "...");
        }
    }

    private static class Task implements Runnable {
        private final Random random = new Random();
        private final SessionFactory sessionFactory;
        private final CountDownLatch latch;
        private final int count;
        private final int index;

        private Task(int index, SessionFactory sessionFactory, int count, CountDownLatch latch) {
            this.sessionFactory = sessionFactory;
            this.latch = latch;
            this.count = count;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                runInSession();

            } finally {
                LOGGER.warn("Reached latch: " + index);
                latch.countDown();
            }
        }

        private void executeTransaction(Session session) {
            Transaction tx = session.beginTransaction();
            try {
                long id = (long) random.nextInt(count);
                DummyEntity dummyEntity = (DummyEntity) session.get(DummyEntity.class, id);
                assertNotNull(dummyEntity, "DummyEntity id=" + id + " not found");

                dummyEntity.setValue(random.nextDouble());

                List<DummyProperty> dummyProperties = dummyEntity.getProperties();
                assertNotNull(dummyProperties, "DummyEntity id=" + id + " does not have properties");

                DummyProperty property = dummyProperties.iterator().next();
                assertNotNull(property, "DummyEntity id=" + id + " does not have first property");

                property.setKey(String.valueOf(random.nextLong()));

                session.save(property);
                session.save(dummyEntity);

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            }
        }

        private void runInSession() {
            Session session = sessionFactory.openSession();
            try {
                for (int i = 0; i < 100; i++) {
                    executeTransaction(session);
                }
            } finally {
                session.close();
            }
        }

        private void assertNotNull(Object object, String msg) {
            if (object == null) {
                throw new NullPointerException(msg);
            }
        }
    }

}
