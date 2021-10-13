public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            new MqPlugin(
                Map.of(
                    "someRabbitQueue", new RabbitTester(
                            5432,
                            new RabbitTester.SendConfig("someRoutingKey"),
                            new RabbitTester.ReceiveConfig("someQueueName")
                    ),
                    "dummyQueue", new MqTester() {
                        private final ArrayDeque<MqTester.Message> queue = new ArrayDeque<>();

                        @Override
                        public void start() {/* open connection*/}

                        @Override
                        public void stop() {/* close connection*/}

                        @Override
                        public void send(MqTester.Message message, Map<String, String>  params) {
                            queue.add(message);
                        }

                        @Override
                        public List<Message> receive() {
                            return queue.stream().map(m -> queue.poll()).collect(Collectors.toList());
                        }

                        @Override
                        public void purge() { queue.clear(); }

                        @Override
                        public boolean accumulateOnRetries() { return true; }
                    }
                )
            )
        );
    }
}