cd "$(dirname "$0")"

echo "🚀 Starting JMeter test..."
echo ""

echo "🧹 Clearing old results..."
rm -f jmeter-results/results.jtl
rm -rf jmeter-results/report
mkdir -p jmeter-results/report
echo ""

docker compose --profile jmeter run --rm jmeter


echo "   - CSV: docker/jmeter-results/results.jtl"
echo "   - HTML Report: docker/jmeter-results/report/index.html"
echo ""

if [ -f "jmeter-results/report/index.html" ]; then
    echo " Opening HTML report..."
    open jmeter-results/report/index.html
fi

