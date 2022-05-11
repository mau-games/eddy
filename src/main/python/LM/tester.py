import tempfile
from datasets.load import dataset_module_factory, import_main_class, load_dataset
from datasets.utils.download_manager import DownloadMode
from datasets.utils.file_utils import DownloadConfig


def test_load_real_dataset(dataset_name):
    path = "./datasets/datasets/" + dataset_name
    dataset_module = dataset_module_factory(path, download_config=DownloadConfig(local_files_only=True))
    builder_cls = import_main_class(dataset_module.module_path)
    name = builder_cls.BUILDER_CONFIGS[0].name if builder_cls.BUILDER_CONFIGS else None
    with tempfile.TemporaryDirectory() as temp_cache_dir:
        dataset = load_dataset(
            path, name=name, cache_dir=temp_cache_dir, download_mode=DownloadMode.FORCE_REDOWNLOAD
        )
        del dataset


def test_load_real_dataset_all_configs(dataset_name):
    path = "./datasets/" + dataset_name
    dataset_module = dataset_module_factory(path, download_config=DownloadConfig(local_files_only=True))
    builder_cls = import_main_class(dataset_module.module_path)
    config_names = (
        [config.name for config in builder_cls.BUILDER_CONFIGS] if len(builder_cls.BUILDER_CONFIGS) > 0 else [None]
    )
    for name in config_names:
        with tempfile.TemporaryDirectory() as temp_cache_dir:
            dataset = load_dataset(
                path, name=name, cache_dir=temp_cache_dir, download_mode=DownloadMode.FORCE_REDOWNLOAD
            )
            del dataset


test_load_real_dataset("nardat")